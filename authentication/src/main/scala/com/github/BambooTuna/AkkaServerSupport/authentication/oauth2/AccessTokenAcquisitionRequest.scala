package com.github.BambooTuna.AkkaServerSupport.authentication.oauth2

trait AccessTokenAcquisitionRequest {
  val grant_type: String
  val code: String
  val redirect_uri: String
  val client_id: String
  val client_secret: String
}
