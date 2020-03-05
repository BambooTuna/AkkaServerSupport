package com.github.BambooTuna.AkkaServerSupport.cooperation.line

import com.github.BambooTuna.AkkaServerSupport.cooperation.model.{
  AccessTokenAcquisitionResponseFailed,
  AccessTokenAcquisitionResponseSuccess
}

case class LineAccessTokenAcquisitionResponseSuccess(
    access_token: String,
    expires_in: Long,
    id_token: Option[String],
    refresh_token: String,
    scope: String,
    token_type: String,
) extends AccessTokenAcquisitionResponseSuccess

case class LineAccessTokenAcquisitionResponseFailed(
    error: String,
    error_description: Option[String],
    error_uri: Option[String])
    extends AccessTokenAcquisitionResponseFailed
