package com.github.BambooTuna.AkkaServerSupport.cooperation.model

trait AccessTokenAcquisitionResponse

trait AccessTokenAcquisitionResponseSuccess
    extends AccessTokenAcquisitionResponse {
  val access_token: String
  val token_type: String
  val expires_in: Long
  val refresh_token: String
}

trait AccessTokenAcquisitionResponseFailed
    extends AccessTokenAcquisitionResponse {
  val error: String
  val error_description: Option[String]
  val error_uri: Option[String]
}
