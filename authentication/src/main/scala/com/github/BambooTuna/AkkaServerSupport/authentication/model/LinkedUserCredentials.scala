package com.github.BambooTuna.AkkaServerSupport.authentication.model

trait LinkedUserCredentials {
  type Id
  type ServiceId
  type ServiceName

  val id: Id
  val serviceId: ServiceId
  val serviceName: ServiceName
}
