package com.github.BambooTuna.AkkaServerSupport.core.error

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, StandardRoute}
import akka.http.scaladsl.server.Directives._
import org.slf4j.Logger

import scala.util.control.NonFatal

//予期せぬ例外のハンドリング

trait CustomExceptionHandler {

  final protected def default(logger: Logger): ExceptionHandler = ExceptionHandler {
    case NonFatal(t) =>
      logger.error(t.getMessage, t)
      complete(StatusCodes.InternalServerError, t.getMessage)
  }

}
