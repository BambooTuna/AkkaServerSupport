package com.github.BambooTuna.AkkaServerSupport.authentication.model

trait LinkedUserCredentials {
  type Id
  type Service

  val id: Id
  val service: Service
}
