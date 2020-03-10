package com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.serializer

import com.github.BambooTuna.AkkaServerSupport.authentication.command.{
  RegisterLinkedUserCredentialsCommand,
  SignInWithLinkageCommand
}
import com.github.BambooTuna.AkkaServerSupport.authentication.error.OAuth2CustomError
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.ClientConfig

trait AccessTokenAcquisitionResponseParser[T] {

  def parseToRegisterCommand(t: T, clientConfig: ClientConfig)
    : Either[OAuth2CustomError, RegisterLinkedUserCredentialsCommand]

  def parseToSignInCommand(t: T, clientConfig: ClientConfig)
    : Either[OAuth2CustomError, SignInWithLinkageCommand]

}
