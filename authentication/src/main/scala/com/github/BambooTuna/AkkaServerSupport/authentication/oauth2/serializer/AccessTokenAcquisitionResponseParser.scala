package com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.serializer

import com.github.BambooTuna.AkkaServerSupport.authentication.command.{RegisterLinkedUserCredentialsCommand, SignInWithLinkageCommand}
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.AccessTokenAcquisitionResponseParserError

trait AccessTokenAcquisitionResponseParser[T] {

  def parseToRegisterCommand(t: T): Either[AccessTokenAcquisitionResponseParserError, RegisterLinkedUserCredentialsCommand]

  def parseToSignInCommand(t: T): Either[AccessTokenAcquisitionResponseParserError, SignInWithLinkageCommand]

}
