package com.github.BambooTuna.AkkaServerSupport.sample.command

import com.github.BambooTuna.AkkaServerSupport.authentication.command.LinkedSignUpInRequestCommand
import com.github.BambooTuna.AkkaServerSupport.sample.model.LinkedUserCredentialsImpl

case class LinkedSignUpInRequestCommandImpl(id: String, service: String)
    extends LinkedSignUpInRequestCommand[LinkedUserCredentialsImpl] {
  override def createLinkedUserCredentials: LinkedUserCredentialsImpl =
    LinkedUserCredentialsImpl(id, service)
}
