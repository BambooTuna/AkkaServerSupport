package com.github.BambooTuna.AkkaServerSupport.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, POST}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.Materializer
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.router.RouteSupport.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  DefaultSession,
  JWTSessionSettings
}
import com.github.BambooTuna.AkkaServerSupport.cooperation.model.{
  ClientConfig,
  OAuth2Settings
}
import com.github.BambooTuna.AkkaServerSupport.core.router.{Router, route}
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import com.github.BambooTuna.AkkaServerSupport.sample.oauth.LineOAuth2RouteImpl
import com.github.BambooTuna.AkkaServerSupport.sample.router.AuthenticationRouteImpl
import doobie.hikari.HikariTransactor
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.ExecutionContext

class Routes(sessionSettings: JWTSessionSettings,
             sessionStorage: StorageStrategy[String, String],
             oauthStorage: StorageStrategy[String, String],
             dbSession: Resource[Task, HikariTransactor[Task]])(
    implicit system: ActorSystem,
    mat: Materializer,
    executor: ExecutionContext) {

  val session =
    new DefaultSession[SessionToken](sessionSettings, sessionStorage) {
      override def fromThrowable(throwable: Throwable): StandardRoute =
        complete(StatusCodes.InternalServerError)
    }

  val authenticationRoute =
    new AuthenticationRouteImpl(session) {
      override def errorHandling(throwable: Throwable): StandardRoute =
        complete(StatusCodes.InternalServerError)
    }

  val lineOAuth = OAuth2Settings(
    ClientConfig.fromConfig("line", system.settings.config),
    oauthStorage)
  val lineOAuth2Route =
    new LineOAuth2RouteImpl(session, lineOAuth) {
      override def errorHandling(throwable: Throwable): StandardRoute =
        complete(StatusCodes.InternalServerError)
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
      route(POST,
            "oauth2" / "signin" / "line",
            lineOAuth2Route.authenticationCodeIssuanceRoute),
      route(
        GET,
        "oauth2" / "signin" / "line",
        lineOAuth2Route.getAccessTokenFromAuthenticationCodeRoute(dbSession))
    )
  }

}
