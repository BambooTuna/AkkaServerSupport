package com.github.BambooTuna.AkkaServerSupport.authentication.session

import scala.concurrent.duration._

class DefaultSessionSettings(override val token: String)
    extends JWTSessionSettings {
  override val setAuthHeaderName: String = "Set-Authorization"
  override val authHeaderName: String = "Authorization"

  override val expirationDate: FiniteDuration = 30.minutes

  override def createTokenId: String =
    java.util.UUID.randomUUID.toString.replaceAll("-", "")
}
