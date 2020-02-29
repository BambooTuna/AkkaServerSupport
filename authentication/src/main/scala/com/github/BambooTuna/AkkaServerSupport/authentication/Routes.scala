package com.github.BambooTuna.AkkaServerSupport.authentication

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.router.AuthenticationRouteImpl
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  DefaultSessionSettings,
  RedisSessionStorageStrategy
}
import com.github.BambooTuna.AkkaServerSupport.core.error.CustomErrorResponse
import com.github.BambooTuna.AkkaServerSupport.core.router.{Router, route}
import com.github.BambooTuna.AkkaServerSupport.core.session.SessionStorageStrategy
import com.github.BambooTuna.AkkaServerSupport.core.session.SessionStorageStrategy.{
  StrategyFindError,
  StrategyRemoveError,
  StrategyStoreError
}
import doobie.hikari.HikariTransactor
import io.circe.Error
import monix.eval.Task

import scala.concurrent.{ExecutionContext, Future}
import monix.execution.Scheduler.Implicits.global
import redis.RedisClient

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
        println(throwable)
        throwable match {
          case e: StrategyStoreError =>
            complete(StatusCodes.InternalServerError, "StrategyStoreError")
          case e: StrategyFindError =>
            complete(StatusCodes.InternalServerError, "StrategyFindError")
          case e: StrategyRemoveError =>
            complete(StatusCodes.InternalServerError, "StrategyRemoveError")
          case e: RuntimeException =>
            complete(StatusCodes.InternalServerError,
                     "Unknown RuntimeException")
          case e: Exception =>
            complete(StatusCodes.BadRequest, "Unknown Exception")
          case e: CustomErrorResponse =>
            complete(e.statusCode, e.toResponseJson)
          case e: Error => complete(StatusCodes.BadRequest, "Unknown Error")
        }
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
