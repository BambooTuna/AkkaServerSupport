package com.github.BambooTuna.AkkaServerSupport.sample

import akka.actor.ActorSystem
import akka.stream.Materializer
import cats.effect.{Blocker, Resource}
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.ClientConfig
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  ConfigSessionSettings,
  DefaultSession,
  JWTSessionSettings,
  SessionToken
}
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  Session,
  StorageStrategy
}
import com.github.BambooTuna.AkkaServerSupport.sample.controller.{
  AuthenticationControllerImpl,
  LineOAuth2ControllerImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.dao.{
  LinkedUserCredentialsDaoImpl,
  RedisStorageStrategy,
  UserCredentialsDaoImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.useCase.{
  AuthenticationUseCaseImpl,
  EmailAuthenticationUseCaseImpl,
  LinkedAuthenticationUseCaseImpl
}
import doobie.hikari.HikariTransactor
import monix.eval.Task
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.mailer.MailerBuilder

import scala.concurrent.ExecutionContext

abstract class Component(implicit system: ActorSystem,
                         mat: Materializer,
                         executor: ExecutionContext) {

  val ec: ExecutionContext = monix.execution.Scheduler.Implicits.global
  val dbSession: Resource[Task, HikariTransactor[Task]] =
    HikariTransactor.newHikariTransactor[Task](
      system.settings.config.getString("mysql.db.driver"),
      system.settings.config.getString("mysql.db.url"),
      system.settings.config.getString("mysql.db.user"),
      system.settings.config.getString("mysql.db.password"),
      ec,
      Blocker.liftExecutionContext(ec)
    )

  val mailer: Mailer =
    MailerBuilder
      .withSMTPServer(
        system.settings.config.getString("mail.host"),
        system.settings.config.getString("mail.port").toInt,
        system.settings.config.getString("mail.username"),
        system.settings.config.getString("mail.password")
      )
      .withTransportStrategy(TransportStrategy.SMTPS)
      .buildMailer()

  private implicit val sessionSettings: JWTSessionSettings =
    new ConfigSessionSettings(system.settings.config)

  val sessionStorage: StorageStrategy[String, String] =
    RedisStorageStrategy.fromConfig(system.settings.config, "session")

  val mailCodeStorage: StorageStrategy[String, String] =
    RedisStorageStrategy.fromConfig(system.settings.config, "mail")

  val oauthStorage: StorageStrategy[String, String] =
    RedisStorageStrategy.fromConfig(system.settings.config, "oauth2")

  private implicit val session: Session[String, SessionToken] =
    new DefaultSession[SessionToken](sessionSettings, sessionStorage)

  private val userCredentialsDao = new UserCredentialsDaoImpl(dbSession)
  private val linkedUserCredentialsDao = new LinkedUserCredentialsDaoImpl(
    dbSession)

  private val authenticationUseCase = new AuthenticationUseCaseImpl(
    userCredentialsDao)
  private val emailAuthenticationUseCase = new EmailAuthenticationUseCaseImpl(
    userCredentialsDao,
    mailCodeStorage,
    mailer)
  private val linkedAuthenticationUseCase = new LinkedAuthenticationUseCaseImpl(
    linkedUserCredentialsDao)

  protected val authenticationController = new AuthenticationControllerImpl(
    authenticationUseCase,
    emailAuthenticationUseCase)

  private val clientConfig: ClientConfig =
    ClientConfig.fromConfig("line", system.settings.config)
  protected val lineOAuth2Controller =
    new LineOAuth2ControllerImpl(linkedAuthenticationUseCase,
                                 clientConfig,
                                 oauthStorage)

}
