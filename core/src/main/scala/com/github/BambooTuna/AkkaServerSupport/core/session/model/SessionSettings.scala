package com.github.BambooTuna.AkkaServerSupport.core.session.model

trait SessionSettings {
  val token: String
  val setAuthHeaderName: String
  val authHeaderName: String

  def createTokenId: String
}
