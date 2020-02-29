package com.github.BambooTuna.AkkaServerSupport.authentication.router.error

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.FailedResponseJson
import com.github.BambooTuna.AkkaServerSupport.core.error.CustomErrorResponse

import io.circe.syntax._
import io.circe.generic.auto._

case class InitializePasswordDataFailedError(message: String)
    extends CustomErrorResponse {
  override val statusCode: StatusCode = StatusCodes.NotFound
  override def toResponseJson: String =
    FailedResponseJson(s"メールアドレスが見つかりません: $message").asJson.noSpaces
}