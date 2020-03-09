package com.github.BambooTuna.AkkaServerSupport.authentication.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Route}
import com.github.BambooTuna.AkkaServerSupport.authentication.error.AuthenticationUseCaseError
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJson,
  SignInRequestJson
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import com.github.BambooTuna.AkkaServerSupport.authentication.session.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
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

  val authenticationUseCase: AuthenticationUseCase[
    SignUpRequest,
    SignInRequest,
    PasswordInitializationRequest,
    Record]
  val dbSession: authenticationUseCase.userCredentialsDao.DBSession

  def signUpRoute(implicit s: Scheduler): QueryP[Unit] = _ {
    entity(as[SignUpRequest]) { json: SignUpRequest =>
      val f: Future[Either[AuthenticationUseCaseError, Record]] =
        authenticationUseCase
          .signUp(json)
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

  def signInRoute(implicit s: Scheduler): QueryP[Unit] = _ {
    entity(as[SignInRequest]) { json: SignInRequest =>
      val f: Future[Either[AuthenticationUseCaseError, Record]] =
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

  def passwordInitializationRoute(implicit s: Scheduler): QueryP[Unit] = _ {
    entity(as[PasswordInitializationRequest]) {
      json: PasswordInitializationRequest =>
        val f: Future[Either[AuthenticationUseCaseError,
                             Record#SigninPass#ValueType]] =
          authenticationUseCase
            .passwordInitialization(json)
            .run(dbSession)
            .runToFuture
        //TODO fの戻り値は新規パスワード、メールに送信などの処理を追加
        onSuccess(f) {
          case Right(value) =>
            complete(StatusCodes.OK,
                     "After signin, please change your password!")
          case Left(value) => reject(value)
        }
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
