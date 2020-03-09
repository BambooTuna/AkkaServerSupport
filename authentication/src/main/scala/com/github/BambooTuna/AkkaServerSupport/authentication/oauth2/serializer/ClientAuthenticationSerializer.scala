package com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.serializer

import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.ClientConfig

trait ClientAuthenticationSerializer[T] {

  def serialize(t: ClientConfig): T

}
