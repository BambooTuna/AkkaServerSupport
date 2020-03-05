package com.github.BambooTuna.AkkaServerSupport.sample.model

import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials

case class LinkedUserCredentialsImpl(id: String,
                                     serviceId: String,
                                     serviceName: String)
    extends LinkedUserCredentials {
  override type Id = String
  override type ServiceId = String
  override type ServiceName = String
}
