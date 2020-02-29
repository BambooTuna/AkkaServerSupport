package com.github.BambooTuna.AkkaServerSupport.authentication

import akka.http.scaladsl.model.HttpMethods._
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.RedisSessionStorageStrategy
import com.github.BambooTuna.AkkaServerSupport.authentication.router.AuthenticationRouteImpl
import com.github.BambooTuna.AkkaServerSupport.core.router.{Router, route}
import com.github.BambooTuna.AkkaServerSupport.core.session.DefaultSessionSettings
import com.github.BambooTuna.AkkaServerSupport.core.session.model.SessionStorageStrategy
import doobie.hikari.HikariTransactor
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
