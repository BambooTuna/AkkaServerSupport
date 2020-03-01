package com.github.BambooTuna.AkkaServerSupport.sample

import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, POST}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  DefaultSessionSettings,
  RedisSessionStorageStrategy
}
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase.AuthenticationUseCaseError
import com.github.BambooTuna.AkkaServerSupport.core.router.{Router, route}
import com.github.BambooTuna.AkkaServerSupport.core.session.SessionStorageStrategy
import com.github.BambooTuna.AkkaServerSupport.sample.router.AuthenticationRouteImpl
import doobie.hikari.HikariTransactor
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import redis.RedisClient

import scala.concurrent.{ExecutionContext, Future}

class Routes(val sessionSettings: DefaultSessionSettings,
             redisSession: RedisClient,
             dbSession: Resource[Task, HikariTransactor[Task]])(
    implicit _executor: ExecutionContext) {

  val authenticationRoute =
    new AuthenticationRouteImpl {
      override def convertIO[O](flow: IO[O]): Future[O] =
        flow.runToFuture
      override implicit val executor: ExecutionContext = _executor
      override implicit val settings: DefaultSessionSettings = sessionSettings
      override implicit val strategy: SessionStorageStrategy[String, String] =
        new RedisSessionStorageStrategy(redisSession)

      override def errorHandling(throwable: Throwable): StandardRoute = {
        throwable match {
          case e: RuntimeException =>
            complete(StatusCodes.InternalServerError, e.getMessage)
          case e: Exception =>
            complete(StatusCodes.BadRequest, e.getMessage)
        }
      }

      override def customErrorHandler(
          error: AuthenticationUseCaseError): StandardRoute = error match {
        case AuthenticationUseCase.SignUpInsertError =>
          complete(StatusCodes.Conflict, "メールアドレスが使用されています")
        case AuthenticationUseCase.CantFoundUserError =>
          complete(StatusCodes.NotFound, "ユーザーが存在しません")
        case AuthenticationUseCase.SignInIdOrPassWrongError =>
          complete(StatusCodes.Forbidden, "認証失敗")
      }
    }

  def createRoute: Router = {

    Router(
      route(POST, "signup", authenticationRoute.signUpRoute(dbSession)),
      route(POST, "signin", authenticationRoute.signInRoute(dbSession)),
      route(POST,
            "init",
            authenticationRoute.passwordInitializationRoute(dbSession)),
      route(GET, "health", authenticationRoute.healthCheck),
      route(DELETE, "logout", authenticationRoute.logout),
    )
  }

}
