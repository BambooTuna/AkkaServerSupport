package com.github.BambooTuna.AkkaServerSupport.core

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.github.BambooTuna.AkkaServerSupport.core.domain.ServerConfig

import scala.concurrent.ExecutionContextExecutor

object Main extends App {

  implicit val system: ActorSystem =
    ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val serverConfig =
    ServerConfig(
      system.settings.config.getString("boot.server.host"),
      system.settings.config.getString("boot.server.port").toInt
    )

  val bindingFuture =
    Http().bindAndHandle(Routes.createRoute.create,
                         serverConfig.host,
                         serverConfig.port)

  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
