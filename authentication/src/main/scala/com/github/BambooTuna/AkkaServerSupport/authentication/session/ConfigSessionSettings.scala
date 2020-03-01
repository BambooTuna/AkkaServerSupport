package com.github.BambooTuna.AkkaServerSupport.authentication.session

import com.typesafe.config.Config

import scala.concurrent.duration._

class ConfigSessionSettings(config: Config) extends JWTSessionSettings {
  override val token: String = config.getString("session.secret")
  override val setAuthHeaderName: String =
    config.getString("session.set_auth_header_name")
  override val authHeaderName: String =
    config.getString("session.auth_header_name")

  override val expirationDate: FiniteDuration = config
    .getDuration("session.expiration_date")
    .getSeconds
    .seconds

  override def createTokenId: String =
    java.util.UUID.randomUUID.toString.replaceAll("-", "")
}
