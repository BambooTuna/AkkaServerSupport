package com.github.BambooTuna.AkkaServerSupport.authentication.command

import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials

trait LinkedSignInRequestCommand[U <: LinkedUserCredentials] {
  val serviceId: U#ServiceId
  val serviceName: U#ServiceName
}
