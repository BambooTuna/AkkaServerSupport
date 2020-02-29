package com.github.BambooTuna.AkkaServerSupport.authentication.router.error

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.FailedResponseJson
import com.github.BambooTuna.AkkaServerSupport.core.error.CustomErrorResponse

import io.circe.syntax._
import io.circe.generic.auto._

case class AuthenticationFailedError(message: String)
    extends CustomErrorResponse {
  override val statusCode: StatusCode = StatusCodes.Forbidden
  override def toResponseJson: String =
    FailedResponseJson(s"認証失敗: $message").asJson.noSpaces
}
