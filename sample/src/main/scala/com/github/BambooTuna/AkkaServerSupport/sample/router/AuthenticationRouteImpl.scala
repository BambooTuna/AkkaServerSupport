package com.github.BambooTuna.AkkaServerSupport.sample.router

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, onComplete}
import akka.http.scaladsl.server.{Directive, Route}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.SuccessResponseJson
import com.github.BambooTuna.AkkaServerSupport.authentication.router.RouteSupport
import com.github.BambooTuna.AkkaServerSupport.authentication.router.RouteSupport.SessionToken
import com.github.BambooTuna.AkkaServerSupport.sample.json.{
  PasswordInitializationRequestJsonImpl,
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import io.circe.syntax._
import io.circe.generic.auto._
import com.github.BambooTuna.AkkaServerSupport.sample.useCase.AuthenticationUseCaseImpl

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait AuthenticationRouteImpl extends RouteSupport {
  type QueryP[Q] = Directive[Q] => Route

  val useCase: AuthenticationUseCaseImpl = new AuthenticationUseCaseImpl

  type DBSession = useCase.userCredentialsDao.DBSession

  type IO[O] = useCase.userCredentialsDao.IO[O]
  type M[O] = useCase.userCredentialsDao.M[O]
  type Id = useCase.userCredentialsDao.Id
  type Record = useCase.userCredentialsDao.Record

  type SignUpRequest = useCase.SignUpRequest
  type SignInRequest = useCase.SignInRequest
  type PasswordInitializationRequest = useCase.PasswordInitializationRequest

  def convertIO[O](flow: IO[O]): Future[O]

  def signUpRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[SignUpRequestJsonImpl]) { json: SignUpRequestJsonImpl =>
      val f =
        useCase
          .signUp(json)
          .run(dbSession)
      onComplete(convertIO(f)) {
        case Success(Right(value)) =>
          session.setSession(SessionToken(value.id)) {
            complete(StatusCodes.OK)
          }
        case Success(Left(value)) => customErrorHandler(value)
        case Failure(e)           => errorHandling(e)
      }
    }
  }

  def signInRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[SignInRequestJsonImpl]) { json: SignInRequestJsonImpl =>
      val f =
        useCase
          .signIn(json)
          .run(dbSession)
      onComplete(convertIO(f)) {
        case Success(Right(value)) =>
          session.setSession(SessionToken(value.id)) {
            complete(StatusCodes.OK)
          }
        case Success(Left(value)) => customErrorHandler(value)
        case Failure(e)           => errorHandling(e)
      }
    }
  }

  def passwordInitializationRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[PasswordInitializationRequestJsonImpl]) {
      json: PasswordInitializationRequestJsonImpl =>
        val f =
          useCase
            .passwordInitialization(json)
            .run(dbSession)

        //TODO fの戻り値は新規パスワード、メールに送信などの処理を追加
        onComplete(convertIO(f)) {
          case Success(Right(value)) =>
            complete(StatusCodes.OK,
                     SuccessResponseJson(
                       "After signin, please change your password!").asJson.noSpaces)
          case Success(Left(value)) => customErrorHandler(value)
          case Failure(e)           => errorHandling(e)
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
