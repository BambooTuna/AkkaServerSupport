package com.github.BambooTuna.AkkaServerSupport.authentication.router.error

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.github.BambooTuna.AkkaServerSupport.core.error.CustomErrorResponse

case class InitializePasswordDataFailedError(message: String)
    extends CustomErrorResponse {
  override val statusCode: StatusCode = StatusCodes.NotFound

  override def toResponseJson: String = message
}
