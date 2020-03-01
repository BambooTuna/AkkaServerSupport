package com.github.BambooTuna.AkkaServerSupport.sample.oauth

import com.github.BambooTuna.AkkaServerSupport.cooperation.twitter.TwitterClientAuthenticationRequest

case class TwitterClientAuthenticationRequestImpl(scope: Option[String]) extends TwitterClientAuthenticationRequest {
  override val client_id: String = ""
  override val redirect_uri: Option[String] = None
}
