package com.github.BambooTuna.AkkaServerSupport.authentication.router.error

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.github.BambooTuna.AkkaServerSupport.core.error.CustomErrorResponse

case class RegisterSignUpDataFailedError(message: String)
    extends CustomErrorResponse {
  override val statusCode: StatusCode = StatusCodes.BadRequest
  override def toResponseJson: String = message
}
