package com.github.BambooTuna.AkkaServerSupport.authentication.oauth2

trait ClientAuthenticationRequest {
  val client_id: String
  val scope: Option[String]
  val state: String
  val redirect_uri: String
  val response_type: String
}
