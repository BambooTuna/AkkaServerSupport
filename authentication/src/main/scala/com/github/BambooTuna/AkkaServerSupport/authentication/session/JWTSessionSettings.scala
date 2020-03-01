package com.github.BambooTuna.AkkaServerSupport.authentication.session

import java.time.Clock

import com.github.BambooTuna.AkkaServerSupport.core.session.SessionSettings
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtHmacAlgorithm

import scala.concurrent.duration._

trait JWTSessionSettings extends SessionSettings {

  val clock: Clock = Clock.systemUTC
  val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256
  val expirationDate: FiniteDuration

}
