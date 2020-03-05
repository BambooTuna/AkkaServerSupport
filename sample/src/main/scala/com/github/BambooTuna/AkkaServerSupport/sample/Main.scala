package com.github.BambooTuna.AkkaServerSupport.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.effect.{Blocker, Resource}
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  ConfigSessionSettings,
  InMemoryStorageStrategy,
  JWTSessionSettings
}
import com.github.BambooTuna.AkkaServerSupport.core.domain.ServerConfig
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import com.github.BambooTuna.AkkaServerSupport.sample.session.RedisStorageStrategy
import doobie.hikari.HikariTransactor
import monix.eval.Task

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

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

  implicit val sessionSettings: JWTSessionSettings =
    new ConfigSessionSettings(system.settings.config)

  val redisSession: StorageStrategy[String, String] =
    RedisStorageStrategy.fromConfig(system.settings.config, "session")

  val redisOAuth: StorageStrategy[String, String] =
    RedisStorageStrategy.fromConfig(system.settings.config, "oauth2")

  val r = new Routes(sessionSettings, redisSession, redisOAuth, dbSession)

  val serverConfig: ServerConfig =
    ServerConfig(
      system.settings.config.getString("boot.server.host"),
      system.settings.config.getString("boot.server.port").toInt
    )

  val bindingFuture =
    Http().bindAndHandle(r.createRoute.create,
                         serverConfig.host,
                         serverConfig.port)

  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
