package com.github.BambooTuna.AkkaServerSupport.authentication

import akka.http.scaladsl.model.HttpMethods._
import com.github.BambooTuna.AkkaServerSupport.authentication.Main.dbSession
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.RedisSessionStorageStrategy
import com.github.BambooTuna.AkkaServerSupport.authentication.router.AuthenticationRouteImpl
import com.github.BambooTuna.AkkaServerSupport.core.router.{Router, route}
import com.github.BambooTuna.AkkaServerSupport.core.session.DefaultSessionSettings
import com.github.BambooTuna.AkkaServerSupport.core.session.model.SessionStorageStrategy

import scala.concurrent.{ExecutionContext, Future}
import monix.execution.Scheduler.Implicits.global
import redis.RedisClient

class Routes(private val sessionSettings: DefaultSessionSettings,
             redisSession: RedisClient)(implicit _executor: ExecutionContext) {

  def createRoute: Router = {
    val myRouter =
      new AuthenticationRouteImpl {
        override def convertIO[O](flow: IO[O]): Future[O] =
          flow.runToFuture
        override implicit val executor: ExecutionContext = _executor
        override implicit val settings: DefaultSessionSettings = sessionSettings
        override implicit val strategy: SessionStorageStrategy[String, String] =
          new RedisSessionStorageStrategy(redisSession)
      }
    Router(
      route(POST, "signup", myRouter.signUpRoute(dbSession)),
      route(POST, "signin", myRouter.signInRoute(dbSession)),
      route(POST, "init", myRouter.passwordInitializationRoute(dbSession)),
      route(GET, "health", myRouter.healthCheck)
    )
  }

}
