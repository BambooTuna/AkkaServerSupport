package com.github.BambooTuna.AkkaServerSupport.core.session

import akka.http.scaladsl.server.{Directive0, Directive1}
import com.github.BambooTuna.AkkaServerSupport.core.session.model.SessionSerializer

import scala.util.Try

trait SessionSettings[K, V] {
  val token: String
  def decodeHeaderValue(value: String): Try[V]
  def findKey(value: String): String

  def setSession(token: V): Directive0
  def requiredSession: Directive1[V]

  def invalidateSession(): Directive0
  def invalidateSession(token: V): Directive0
}
