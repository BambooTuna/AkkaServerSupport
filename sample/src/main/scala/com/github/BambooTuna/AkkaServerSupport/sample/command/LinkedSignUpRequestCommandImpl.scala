package com.github.BambooTuna.AkkaServerSupport.sample.command

import com.github.BambooTuna.AkkaServerSupport.authentication.command.LinkedSignUpRequestCommand
import com.github.BambooTuna.AkkaServerSupport.sample.SystemSettings
import com.github.BambooTuna.AkkaServerSupport.sample.model.LinkedUserCredentialsImpl

case class LinkedSignUpRequestCommandImpl(serviceId: String,
                                          serviceName: String)
    extends LinkedSignUpRequestCommand[LinkedUserCredentialsImpl] {
  override def createLinkedUserCredentials: LinkedUserCredentialsImpl =
    LinkedUserCredentialsImpl(SystemSettings.generateId(),
                              serviceId,
                              serviceName)
}
