package com.github.BambooTuna.AkkaServerSupport.core.session.model

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Directive0, Directive1}
import akka.http.scaladsl.server.Directives.respondWithHeaders

trait Session[K, V] {

  val settings: SessionSettings

  def setSession(token: V): Directive0
  def requiredSession: Directive1[V]
  def invalidateSession(): Directive0
  def invalidateSession(value: String): Directive0

  def addAuthHeader(token: String): Directive0 =
    respondWithHeaders(RawHeader(settings.authHeaderName, token))

}
