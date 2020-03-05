package com.github.BambooTuna.AkkaServerSupport.cooperation.model

sealed trait ClientAuthenticationResponse {
  val state: String
}

trait ClientAuthenticationResponseSuccess extends ClientAuthenticationResponse {
  val code: String
}

trait ClientAuthenticationResponseFailed extends ClientAuthenticationResponse {
  val error: String
  val error_description: Option[String]
  val error_uri: Option[String]
}
