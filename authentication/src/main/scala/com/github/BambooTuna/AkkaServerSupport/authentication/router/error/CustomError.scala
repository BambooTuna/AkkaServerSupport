package com.github.BambooTuna.AkkaServerSupport.authentication.router.error

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

trait CustomError {
  val statusCode: StatusCode
  val message: Option[String]
}

case class RuntimeExceptionError(mes: String) extends CustomError {
  override val statusCode: StatusCode = StatusCodes.InternalServerError
  override val message: Option[String] = Some(mes)
}
