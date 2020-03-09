package com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.serializer

import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.ClientConfig

trait AccessTokenAcquisitionSerializer[T] {

  def serialize(t: ClientConfig, code: String): T

}
