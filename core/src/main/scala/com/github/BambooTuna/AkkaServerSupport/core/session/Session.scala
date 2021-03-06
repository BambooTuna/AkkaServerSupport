package com.github.BambooTuna.AkkaServerSupport.core.session

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.respondWithHeaders
import akka.http.scaladsl.server.{Directive0, Directive1}

trait Session[K, V] {

  val settings: SessionSettings
  val strategy: StorageStrategy[K, String]

  def setSession(token: V): Directive0
  def requiredSession: Directive1[V]
  def optionalRequiredSession: Directive1[Option[V]]
  def invalidateSession(): Directive0
  def invalidateSession(value: String): Directive0

  def addAuthHeader(token: String): Directive0 =
    respondWithHeaders(RawHeader(settings.setAuthHeaderName, token))

}
