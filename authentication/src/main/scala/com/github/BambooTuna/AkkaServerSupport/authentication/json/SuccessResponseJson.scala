package com.github.BambooTuna.AkkaServerSupport.authentication.json

case class SuccessResponseJson(message: String) extends ResponseJson {
  override val result: String = "success"
}
