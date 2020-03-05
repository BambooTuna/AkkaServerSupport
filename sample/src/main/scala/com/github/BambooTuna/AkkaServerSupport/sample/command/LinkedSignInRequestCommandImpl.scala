package com.github.BambooTuna.AkkaServerSupport.sample.command

import com.github.BambooTuna.AkkaServerSupport.authentication.command.LinkedSignInRequestCommand
import com.github.BambooTuna.AkkaServerSupport.sample.model.LinkedUserCredentialsImpl

case class LinkedSignInRequestCommandImpl(serviceId: String,
                                          serviceName: String)
    extends LinkedSignInRequestCommand[LinkedUserCredentialsImpl]
