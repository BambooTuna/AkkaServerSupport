package com.github.BambooTuna.AkkaServerSupport.authentication.json

case class FailedResponseJson(message: String) extends ResponseJson {
  override val result: String = "failed"
}
