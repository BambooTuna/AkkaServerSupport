package com.github.BambooTuna.AkkaServerSupport.authentication.router.error

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.github.BambooTuna.AkkaServerSupport.core.error.CustomErrorResponse

case class AuthenticationFailedError(message: String)
    extends CustomErrorResponse {
  override val statusCode: StatusCode = StatusCodes.Forbidden
  override def toResponseJson: String = "認証失敗しました"
}
