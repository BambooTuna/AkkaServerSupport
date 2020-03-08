package com.github.BambooTuna.AkkaServerSupport.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, POST}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.ClientConfig
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  DefaultSession,
  JWTSessionSettings,
  SessionToken
}
import com.github.BambooTuna.AkkaServerSupport.core.router.{
  RouteController,
  Router,
  route
}
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  Session,
  StorageStrategy
}
import com.github.BambooTuna.AkkaServerSupport.sample.controller.{
  AuthenticationControllerImpl,
  LineOAuth2ControllerImpl
}
import doobie.hikari.HikariTransactor
import monix.eval.Task
import monix.execution.Scheduler
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

class RouteControllerImpl(sessionSettings: JWTSessionSettings,
                          sessionStorage: StorageStrategy[String, String],
                          oauthStorage: StorageStrategy[String, String],
                          dbSession: Resource[Task, HikariTransactor[Task]])(
    implicit system: ActorSystem,
    mat: Materializer,
    executor: ExecutionContext)
    extends RouteController {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  override def toRoutes: Route =
    handleExceptions(defaultExceptionHandler(logger)) {
      handleRejections(defaultRejectionHandler) {
        createRoute(monix.execution.Scheduler.Implicits.global).create
      }
    }

  private implicit val session: Session[String, SessionToken] =
    new DefaultSession[SessionToken](sessionSettings, sessionStorage)

  private val authenticationController =
    new AuthenticationControllerImpl(dbSession)

  private val clientConfig: ClientConfig =
    ClientConfig.fromConfig("line", system.settings.config)
  private val lineOAuth2Controller =
    new LineOAuth2ControllerImpl(clientConfig, oauthStorage, dbSession)

  def createRoute(implicit s: Scheduler): Router = {
    Router(
      route(POST, "signup", authenticationController.signUpRoute),
      route(POST, "signin", authenticationController.signInRoute),
      route(POST, "init", authenticationController.passwordInitializationRoute),
      route(GET, "health", authenticationController.healthCheck),
      route(DELETE, "logout", authenticationController.logout),
      route(POST,
            "oauth2" / "signin" / "line",
            lineOAuth2Controller.fetchCooperationLink),
      route(GET,
            "oauth2" / "signin" / "line",
            lineOAuth2Controller.authenticationFromCode)
    )
  }

}
