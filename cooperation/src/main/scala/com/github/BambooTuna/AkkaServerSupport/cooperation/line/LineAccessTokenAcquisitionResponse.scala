package com.github.BambooTuna.AkkaServerSupport.cooperation.line

import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.AccessTokenAcquisitionResponse

case class LineAccessTokenAcquisitionResponse(
    access_token: String,
    expires_in: Long,
    id_token: Option[String],
    refresh_token: String,
    scope: String,
    token_type: String,
) extends AccessTokenAcquisitionResponse
