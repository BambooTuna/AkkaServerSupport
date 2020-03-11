package com.github.BambooTuna.AkkaServerSupport.authentication.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Route}
import cats.data.EitherT
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

  def signUpRoute(
      dbSession: authenticationUseCase.userCredentialsDao.DBSession)(
      implicit s: Scheduler): QueryP[Unit] = _ {
    entity(as[SignUpRequest]) { json: SignUpRequest =>
      val f: Future[Either[AuthenticationCustomError, Record]] =
        (for {
          recode <- EitherT {
            authenticationUseCase.signUp(json).run(dbSession)
          }
          _ <- EitherT {
            emailAuthenticationUseCase
              .issueActivateCode(recode.id)
              .run(dbSession.asInstanceOf[
                emailAuthenticationUseCase.userCredentialsDao.DBSession])
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

  def issueActivateCodeRoute(
      dbSession: emailAuthenticationUseCase.userCredentialsDao.DBSession)(
      implicit s: Scheduler): QueryP[Unit] = _ {
    session.requiredSession {
      case SessionToken(_, Some(_)) => complete(StatusCodes.NotFound)
      case SessionToken(userId, None) =>
        val f: Future[Either[AuthenticationCustomError, Unit]] =
          emailAuthenticationUseCase
            .issueActivateCode(userId)
            .run(dbSession)
            .runToFuture
        onSuccess(f) {
          case Right(_)    => complete(StatusCodes.OK)
          case Left(value) => reject(value)
        }
    }
  }

  def activateAccountRoute(
      dbSession: emailAuthenticationUseCase.userCredentialsDao.DBSession)(
      implicit s: Scheduler): QueryP[Tuple1[String]] = _ { code =>
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

  def signInRoute(
      dbSession: authenticationUseCase.userCredentialsDao.DBSession)(
      implicit s: Scheduler): QueryP[Unit] = _ {
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

  def tryInitializationRoute(
      dbSession: emailAuthenticationUseCase.userCredentialsDao.DBSession)(
      implicit s: Scheduler): QueryP[Unit] = _ {
    entity(as[PasswordInitializationRequest]) {
      json: PasswordInitializationRequest =>
        val f: Future[Either[AuthenticationCustomError, Unit]] =
          emailAuthenticationUseCase
            .issueInitializationCode(json.signInId)
            .run(dbSession)
            .runToFuture
        onSuccess(f) {
          case Right(_) =>
            complete(StatusCodes.OK)
          case Left(value) => reject(value)
        }
    }
  }

  def initAccountPassword(
      dbSession: emailAuthenticationUseCase.userCredentialsDao.DBSession)(
      implicit s: Scheduler): QueryP[Tuple1[String]] = _ { code =>
    val f: Future[Either[AuthenticationCustomError, Unit]] =
      emailAuthenticationUseCase
        .initAccountPassword(code)
        .run(dbSession)
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
