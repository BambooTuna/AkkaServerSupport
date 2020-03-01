package com.github.BambooTuna.AkkaServerSupport.cooperation.twitter

import com.github.BambooTuna.AkkaServerSupport.cooperation.model.ClientAuthenticationRequest

trait TwitterClientAuthenticationRequest extends ClientAuthenticationRequest {
  override val state: Option[String] = Some(generateState)
  override val response_type: String = "code"
}
