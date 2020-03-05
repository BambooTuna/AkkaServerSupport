package com.github.BambooTuna.AkkaServerSupport.authentication.command

import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials

trait LinkedSignUpRequestCommand[U <: LinkedUserCredentials] {
  def createLinkedUserCredentials: U
}
