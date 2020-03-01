package com.github.BambooTuna.AkkaServerSupport.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.effect.{Blocker, Resource}
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  ConfigSessionSettings,
  JWTSessionSettings
}
import com.github.BambooTuna.AkkaServerSupport.core.domain.ServerConfig
import doobie.hikari.HikariTransactor
import monix.eval.Task
import redis.RedisClient

import scala.concurrent.duration._
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

  val redisSession: RedisClient =
    RedisClient(
      host = system.settings.config.getString("redis.db.host"),
      port = system.settings.config.getInt("redis.db.port"),
      password = Some(system.settings.config.getString("redis.db.password"))
        .filter(_.nonEmpty),
      db = Some(system.settings.config.getInt("redis.db.db")),
      connectTimeout = Some(
        system.settings.config
          .getDuration("redis.db.connect-timeout")
          .toMillis
          .millis)
    )

  val serverConfig: ServerConfig =
    ServerConfig(
      system.settings.config.getString("boot.server.host"),
      system.settings.config.getString("boot.server.port").toInt
    )

  val sessionSettings: JWTSessionSettings =
    new ConfigSessionSettings(system.settings.config)

  val r = new Routes(sessionSettings, redisSession, dbSession)

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
