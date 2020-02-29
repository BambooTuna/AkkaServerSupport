package com.github.BambooTuna.AkkaServerSupport.core.session

trait SessionSettings {
  val token: String
  val setAuthHeaderName: String
  val authHeaderName: String

  def createTokenId: String
}
