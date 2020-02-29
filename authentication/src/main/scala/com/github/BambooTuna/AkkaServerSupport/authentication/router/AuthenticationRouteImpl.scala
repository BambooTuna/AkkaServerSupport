package com.github.BambooTuna.AkkaServerSupport.authentication.router

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive, Route}
import akka.http.scaladsl.server.Directives._
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJsonImpl,
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl,
  SuccessResponseJson
}
import com.github.BambooTuna.AkkaServerSupport.authentication.router.RouteSupport.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.router.error.{
  AuthenticationFailedError,
  InitializePasswordDataFailedError,
  RegisterSignUpDataFailedError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCaseImpl

import scala.concurrent.Future
import scala.util.{Failure, Success}

import io.circe.syntax._
import io.circe.generic.auto._
import monix.eval.Task

trait AuthenticationRouteImpl extends RouteSupport {
  type QueryP[Q] = Directive[Q] => Route

  val useCase: AuthenticationUseCaseImpl = new AuthenticationUseCaseImpl

  type IO[O] = useCase.userCredentialsDao.IO[O]
  type DBSession = useCase.userCredentialsDao.DBSession

  type M[O] = useCase.userCredentialsDao.M[O]
  type Id = useCase.userCredentialsDao.Id
  type Record = useCase.userCredentialsDao.Record

  type SignUpRequest = useCase.SignUpRequest
  type SignInRequest = useCase.SignInRequest
  type PasswordInitializationRequest = useCase.PasswordInitializationRequest

  def convertIO[O](flow: IO[O]): Future[O]

  def signUpRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[SignUpRequestJsonImpl]) { json: SignUpRequestJsonImpl =>
      val f: IO[Option[Record]] =
        useCase
          .signUp(json)
          .value
          .run(dbSession)
          .onErrorHandleWith(_ =>
            Task.raiseError(
              RegisterSignUpDataFailedError(s"メールアドレスが使われています: ${json.mail}")))
      onComplete(convertIO[Option[Record]](f)) {
        case Success(Some(value)) =>
          session.setSession(SessionToken(value.id)) {
            complete(StatusCodes.OK)
          }
        case Success(None) =>
          errorHandling(
            new RuntimeException("DB Access Future Error in signUpRoute"))
        case Failure(e) => errorHandling(e)
      }
    }
  }

  def signInRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[SignInRequestJsonImpl]) { json: SignInRequestJsonImpl =>
      val f: IO[Option[Record]] =
        useCase
          .signIn(json)
          .value
          .run(dbSession)
          .onErrorHandleWith(_ =>
            Task.raiseError(
              new RuntimeException("DB Access Future Error in signInRoute")))
      onComplete(convertIO[Option[Record]](f)) {
        case Success(Some(value)) =>
          session.setSession(SessionToken(value.id)) {
            complete(StatusCodes.OK)
          }
        case Success(None) =>
          errorHandling(AuthenticationFailedError(s"メールアドレスかパスワードが間違っています"))
        case Failure(e) => errorHandling(e)
      }
    }
  }

  def passwordInitializationRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[PasswordInitializationRequestJsonImpl]) {
      json: PasswordInitializationRequestJsonImpl =>
        val f: IO[Option[String]] =
          useCase
            .passwordInitialization(json)
            .value
            .run(dbSession)
            .onErrorHandleWith(_ =>
              Task.raiseError(new RuntimeException(
                "DB Access Future Error in passwordInitializationRoute"))) //ここでFutureエラーが起きる＝深刻なエラー

        //TODO fの戻り値は新規パスワード、メールに送信などの処理を追加
        onComplete(convertIO[Option[String]](f)) {
          case Success(Some(value)) =>
            complete(StatusCodes.OK,
                     SuccessResponseJson(
                       "After signin, please change your password!").asJson.noSpaces)
          case Success(None) =>
            errorHandling(InitializePasswordDataFailedError(json.mail))
          case Failure(e) => errorHandling(e)
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
