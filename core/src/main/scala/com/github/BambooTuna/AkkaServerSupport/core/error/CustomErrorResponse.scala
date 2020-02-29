package com.github.BambooTuna.AkkaServerSupport.core.error

import akka.http.scaladsl.model.StatusCode

trait CustomErrorResponse {
  val message: String
  val statusCode: StatusCode
  def toResponseJson: String
}
