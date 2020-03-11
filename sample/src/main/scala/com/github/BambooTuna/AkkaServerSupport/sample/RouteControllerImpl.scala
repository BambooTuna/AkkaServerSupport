package com.github.BambooTuna.AkkaServerSupport.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, POST, PUT}
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
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

class RouteControllerImpl(sessionSettings: JWTSessionSettings,
                          sessionStorage: StorageStrategy[String, String],
                          mailCodeStorage: StorageStrategy[String, String],
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
        (
          authenticationCodeCycleRoute(
            monix.execution.Scheduler.Implicits.global) +
            accountCycleRoute(monix.execution.Scheduler.Implicits.global) +
            oauth2Route(monix.execution.Scheduler.Implicits.global)
        ).create
      }
    }

  system.settings.config.getString("mail.host")

  val mailer = MailerBuilder
    .withSMTPServer(
      system.settings.config.getString("mail.host"),
      system.settings.config.getString("mail.port").toInt,
      system.settings.config.getString("mail.username"),
      system.settings.config.getString("mail.password")
    )
    .withTransportStrategy(TransportStrategy.SMTPS)
    .buildMailer()

  private implicit val session: Session[String, SessionToken] =
    new DefaultSession[SessionToken](sessionSettings, sessionStorage)

  private val authenticationController =
    new AuthenticationControllerImpl(dbSession, mailer, mailCodeStorage)

  private val clientConfig: ClientConfig =
    ClientConfig.fromConfig("line", system.settings.config)
  private val lineOAuth2Controller =
    new LineOAuth2ControllerImpl(clientConfig, oauthStorage, dbSession)

  def authenticationCodeCycleRoute(implicit s: Scheduler): Router = {
    Router(
      route(PUT,
            "activate",
            authenticationController.issueActivateCodeRoute(dbSession)),
      route(GET,
            "activate" / Segment,
            authenticationController.activateAccountRoute(dbSession)),
      route(POST,
            "init",
            authenticationController.tryInitializationRoute(dbSession)),
      route(GET,
            "init" / Segment,
            authenticationController.initAccountPassword(dbSession))
    )
  }

  def accountCycleRoute(implicit s: Scheduler): Router =
    Router(
      route(POST, "signup", authenticationController.signUpRoute(dbSession)),
      route(POST, "signin", authenticationController.signInRoute(dbSession)),
      route(GET, "health", authenticationController.healthCheck),
      route(DELETE, "logout", authenticationController.logout)
    )

  def oauth2Route(implicit s: Scheduler): Router =
    Router(
      route(POST,
            "oauth2" / "signin" / "line",
            lineOAuth2Controller.fetchCooperationLink),
      route(GET,
            "oauth2" / "signin" / "line",
            lineOAuth2Controller.authenticationFromCode(dbSession))
    )

}
