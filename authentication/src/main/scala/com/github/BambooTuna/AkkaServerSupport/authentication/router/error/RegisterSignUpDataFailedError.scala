package com.github.BambooTuna.AkkaServerSupport.authentication.router.error

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.FailedResponseJson
import com.github.BambooTuna.AkkaServerSupport.core.error.CustomErrorResponse
import io.circe.syntax._
import io.circe.generic.auto._

case class RegisterSignUpDataFailedError(message: String)
    extends CustomErrorResponse {
  override val statusCode: StatusCode = StatusCodes.BadRequest
  override def toResponseJson: String =
    FailedResponseJson(message).asJson.noSpaces
}
