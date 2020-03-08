package com.github.BambooTuna.AkkaServerSupport.authentication.oauth2

trait AccessTokenAcquisitionResponse {
  val access_token: String
  val token_type: String
  val expires_in: Long
  val refresh_token: String
}
