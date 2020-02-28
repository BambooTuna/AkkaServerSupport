package com.github.BambooTuna.AkkaServerSupport.core.session

import java.time.Clock

import com.github.BambooTuna.AkkaServerSupport.core.session.model.SessionSettings
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtHmacAlgorithm

import scala.concurrent.duration._

class DefaultSessionSettings(override val token: String)
    extends SessionSettings {
  override val setAuthHeaderName: String = "Set-Authorization"
  override val authHeaderName: String = "Authorization"

  val clock: Clock = Clock.systemUTC
  val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256
  val expirationDate: FiniteDuration = 30.minutes

  override def createTokenId: String =
    java.util.UUID.randomUUID.toString.replaceAll("-", "")
}
