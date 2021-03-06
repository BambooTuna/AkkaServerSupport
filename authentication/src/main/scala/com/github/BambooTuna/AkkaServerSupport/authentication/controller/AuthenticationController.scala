package com.github.BambooTuna.AkkaServerSupport.authentication.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Route}
import com.github.BambooTuna.AkkaServerSupport.authentication.error.AuthenticationCustomError
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
import com.github.BambooTuna.AkkaServerSupport.core.session.Session
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Decoder
import monix.execution.Scheduler

import scala.concurrent.Future

abstract class AuthenticationController[
    SignUpRequest,
    SignInRequest <: SignInRequestJson,
    PasswordInitializationRequest <: PasswordInitializationRequestJson,
    Record <: UserCredentials](implicit session: Session[String, SessionToken],
                               su: Decoder[SignUpRequest],
                               si: Decoder[SignInRequest],
                               pi: Decoder[PasswordInitializationRequest])
    extends FailFastCirceSupport {

  type QueryP[Q] = Directive[Q] => Route

  val authenticationUseCase: AuthenticationUseCase[SignUpRequest,
                                                   SignInRequest,
                                                   Record]
  val emailAuthenticationUseCase: EmailAuthenticationUseCase[Record]

  def signUpRoute(implicit s: Scheduler): QueryP[Unit] = _ {
    entity(as[SignUpRequest]) { json: SignUpRequest =>
      val f: Future[Either[AuthenticationCustomError, Record]] =
        (for {
          recode <- authenticationUseCase.signUp(json)
          _ <- emailAuthenticationUseCase.issueActivateCode(recode.id)
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

  def issueActivateCodeRoute(implicit s: Scheduler): QueryP[Unit] = _ {
    session.requiredSession {
      case SessionToken(_, Some(_)) => complete(StatusCodes.NotFound)
      case SessionToken(userId, None) =>
        val f: Future[Either[AuthenticationCustomError, Unit]] =
          emailAuthenticationUseCase
            .issueActivateCode(userId)
            .value
            .runToFuture
        onSuccess(f) {
          case Right(_)    => complete(StatusCodes.OK)
          case Left(value) => reject(value)
        }
    }
  }

  def activateAccountRoute(implicit s: Scheduler): QueryP[Tuple1[String]] = _ {
    code =>
      val f: Future[Either[AuthenticationCustomError, Unit]] =
        emailAuthenticationUseCase
          .activateAccount(code)
          .value
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
          .value
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
            .value
            .runToFuture
        onSuccess(f) {
          case Right(_) =>
            complete(StatusCodes.OK)
          case Left(value) => reject(value)
        }
    }
  }

  def initAccountPassword(implicit s: Scheduler): QueryP[Tuple1[String]] = _ {
    code =>
      val f: Future[Either[AuthenticationCustomError, Unit]] =
        emailAuthenticationUseCase
          .initAccountPassword(code)
          .value
          .runToFuture
      onSuccess(f) {
        case Right(_)    => complete(StatusCodes.OK)
        case Left(value) => reject(value)
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
