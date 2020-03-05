package com.github.BambooTuna.AkkaServerSupport.cooperation.line

import com.github.BambooTuna.AkkaServerSupport.cooperation.model.{
  ClientAuthenticationResponseFailed,
  ClientAuthenticationResponseSuccess
}

case class LineClientAuthenticationResponseSuccess(code: String, state: String)
    extends ClientAuthenticationResponseSuccess

case class LineClientAuthenticationResponseFailed(
    error: String,
    error_description: Option[String],
    error_uri: Option[String],
    state: String
) extends ClientAuthenticationResponseFailed
