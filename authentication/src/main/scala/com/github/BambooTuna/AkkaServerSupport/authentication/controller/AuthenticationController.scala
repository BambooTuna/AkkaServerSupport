package com.github.BambooTuna.AkkaServerSupport.authentication.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Route}
import cats.data.{EitherT, OptionT}
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AuthenticationCustomError,
  InvalidActivateCodeError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJson,
  SignInRequestJson
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import com.github.BambooTuna.AkkaServerSupport.authentication.session.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.{
  AuthenticationUseCase,
  EmailAuthenticationUseCase
}
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  Session,
  StorageStrategy
}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Decoder
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Future

abstract class AuthenticationController[
    DBSession,
    SignUpRequest,
    SignInRequest <: SignInRequestJson,
    PasswordInitializationRequest <: PasswordInitializationRequestJson,
    Record <: UserCredentials](implicit session: Session[String, SessionToken],
                               su: Decoder[SignUpRequest],
                               si: Decoder[SignInRequest],
                               pi: Decoder[PasswordInitializationRequest])
    extends FailFastCirceSupport {

  type QueryP[Q] = Directive[Q] => Route

  val authenticationUseCase: AuthenticationUseCase[
    DBSession,
    SignUpRequest,
    SignInRequest,
    PasswordInitializationRequest,
    Record]
  val emailAuthenticationUseCase: EmailAuthenticationUseCase[DBSession, Record]

  val dbSession: DBSession

  def signUpRoute(implicit s: Scheduler): QueryP[Unit] = _ {
    entity(as[SignUpRequest]) { json: SignUpRequest =>
      val f: Future[Either[AuthenticationCustomError, Record]] =
        (for {
          recode <- EitherT {
            authenticationUseCase.signUp(json).run(dbSession)
          }
          _ <- EitherT[Task, AuthenticationCustomError, Unit] {
            emailAuthenticationUseCase
              .issueActivateCode(recode.id, recode.signinId)
              .map(Right(_))
          }
        } yield recode).value.runToFuture
      onSuccess(f) {
        case Right(value) =>
          session.setSession(SessionToken(value.id)) {
            complete(StatusCodes.OK)
          }
        case Left(value) => reject(value)
      }
    }
  }

  def activateAccountRoute(implicit s: Scheduler): QueryP[Tuple1[String]] = _ {
    code =>
      val f: Future[Either[AuthenticationCustomError, Unit]] =
        emailAuthenticationUseCase
          .activateAccount(code)
          .run(dbSession)
          .runToFuture
      onSuccess(f) {
        case Right(_)    => complete(StatusCodes.OK)
        case Left(value) => reject(value)
      }
  }

  def signInRoute(implicit s: Scheduler): QueryP[Unit] = _ {
    entity(as[SignInRequest]) { json: SignInRequest =>
      val f: Future[Either[AuthenticationCustomError, Record]] =
        authenticationUseCase
          .signIn(json)
          .run(dbSession)
          .runToFuture
      onSuccess(f) {
        case Right(value) =>
          session.setSession(SessionToken(value.id)) {
            complete(StatusCodes.OK)
          }
        case Left(value) => reject(value)
      }
    }
  }

  def tryInitializationRoute(implicit s: Scheduler): QueryP[Unit] = _ {
    entity(as[PasswordInitializationRequest]) {
      json: PasswordInitializationRequest =>
        val f: Future[Either[AuthenticationCustomError, Unit]] =
          emailAuthenticationUseCase
            .issueInitializationCode(json.signInId)
            .run(dbSession)
            .runToFuture
        onSuccess(f) {
          case Right(_) =>
            complete(StatusCodes.OK, "メールに初期化用のリンクを送りました")
          case Left(value) => reject(value)
        }
    }
  }

  def initAccountPassword(implicit s: Scheduler): QueryP[Tuple1[String]] = _ {
    code =>
      val f: Future[Either[AuthenticationCustomError, String]] =
        emailAuthenticationUseCase
          .initAccountPassword(code)
          .run(dbSession)
          .runToFuture
      onSuccess(f) {
        case Right(value) => complete(StatusCodes.OK, s"新しいパスワード: $value")
        case Left(value)  => reject(value)
      }
  }

  def healthCheck: QueryP[Unit] = _ {
    session.requiredSession { _ =>
      complete(StatusCodes.OK)
    }
  }

  def logout: QueryP[Unit] = _ {
    session.invalidateSession() {
      complete(StatusCodes.OK)
    }
  }

}
