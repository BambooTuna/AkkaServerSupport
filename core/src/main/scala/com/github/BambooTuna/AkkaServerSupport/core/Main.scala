package com.github.BambooTuna.AkkaServerSupport.core

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.{GET, PUT}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import cats.data.OptionT

import scala.concurrent.{ExecutionContextExecutor, Future}

object Main extends App {

  implicit val system: ActorSystem =
    ActorSystem("ShelterSearcherServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  OptionT[Future, Int] { Future(Seq(1, 12, 3).headOption) }.value
    .onComplete(println)

//  Router(
//    route(GET, "server", get { complete(StatusCodes.OK) })
//  )

}
