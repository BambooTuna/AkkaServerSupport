package com.github.BambooTuna.AkkaServerSupport.authentication.command

import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials

trait LinkedSignUpInRequestCommand[U <: LinkedUserCredentials] {
  val id: U#Id
  val service: U#Service

  def createLinkedUserCredentials: U
}
