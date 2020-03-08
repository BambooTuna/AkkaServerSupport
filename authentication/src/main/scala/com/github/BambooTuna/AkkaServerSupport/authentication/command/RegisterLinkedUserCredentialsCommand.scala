package com.github.BambooTuna.AkkaServerSupport.authentication.command

import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials

trait RegisterLinkedUserCredentialsCommand {
  def createLinkedUserCredentials: LinkedUserCredentials
}
