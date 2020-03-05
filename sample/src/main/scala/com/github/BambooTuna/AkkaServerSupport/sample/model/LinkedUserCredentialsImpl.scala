package com.github.BambooTuna.AkkaServerSupport.sample.model

import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials

case class LinkedUserCredentialsImpl(id: String, service: String)
    extends LinkedUserCredentials {
  override type Id = String
  override type Service = String
}
