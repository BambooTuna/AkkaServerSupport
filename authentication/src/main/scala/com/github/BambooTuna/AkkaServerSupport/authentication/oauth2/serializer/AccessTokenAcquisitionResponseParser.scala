package com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.serializer

import com.github.BambooTuna.AkkaServerSupport.authentication.command.{
  RegisterLinkedUserCredentialsCommand,
  SignInWithLinkageCommand
}
import com.github.BambooTuna.AkkaServerSupport.authentication.error.AccessTokenAcquisitionResponseParserError
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.ClientConfig

trait AccessTokenAcquisitionResponseParser[T] {

  def parseToRegisterCommand(t: T, clientConfig: ClientConfig)
    : Either[AccessTokenAcquisitionResponseParserError,
             RegisterLinkedUserCredentialsCommand]

  def parseToSignInCommand(t: T, clientConfig: ClientConfig)
    : Either[AccessTokenAcquisitionResponseParserError,
             SignInWithLinkageCommand]

}
