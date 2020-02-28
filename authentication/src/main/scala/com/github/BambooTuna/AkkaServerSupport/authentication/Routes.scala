package com.github.BambooTuna.AkkaServerSupport.authentication

import akka.http.scaladsl.model.HttpMethods.POST
import com.github.BambooTuna.AkkaServerSupport.authentication.Main.dbSession
import com.github.BambooTuna.AkkaServerSupport.authentication.router.AuthenticationRouteImpl
import com.github.BambooTuna.AkkaServerSupport.core.router.{Router, route}

import scala.concurrent.Future
import monix.execution.Scheduler.Implicits.global

object Routes {

  def createRoute: Router = {
    val myRouter =
      new AuthenticationRouteImpl {
        override def convertIO[O](flow: IO[O]): Future[O] =
          flow.runToFuture
      }
    Router(
      route(POST, "signup", myRouter.signUpRoute(dbSession)),
      route(POST, "signin", myRouter.signInRoute(dbSession)),
      route(POST, "init", myRouter.passwordInitializationRoute(dbSession))
    )
  }

}
