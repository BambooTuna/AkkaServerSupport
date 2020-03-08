package com.github.BambooTuna.AkkaServerSupport.cooperation.line

import com.github.BambooTuna.AkkaServerSupport.authentication.command.{
  RegisterLinkedUserCredentialsCommand,
  SignInWithLinkageCommand
}
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AccessTokenAcquisitionResponseParserError,
  ParseToRegisterCommandError,
  ParseToSignInCommandError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.{
  AccessTokenAcquisitionResponse,
  ClientConfig
}
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.serializer.AccessTokenAcquisitionResponseParser
import pdi.jwt.{Jwt, JwtAlgorithm}

case class LineAccessTokenAcquisitionResponse(
    access_token: String,
    expires_in: Long,
    id_token: Option[String],
    refresh_token: String,
    scope: String,
    token_type: String,
) extends AccessTokenAcquisitionResponse

object LineAccessTokenAcquisitionResponse {
  implicit val a =
    new AccessTokenAcquisitionResponseParser[LineAccessTokenAcquisitionResponse] {

      def decodeIdToken(t: LineAccessTokenAcquisitionResponse,
                        clientConfig: ClientConfig): Option[String] = {
        t.id_token
          .flatMap(
            a =>
              Jwt
                .decode(a, clientConfig.clientSecret, Seq(JwtAlgorithm.HS256))
                .toOption)
          .flatMap(_.subject)
      }

      override def parseToRegisterCommand(t: LineAccessTokenAcquisitionResponse,
                                          clientConfig: ClientConfig)
        : Either[AccessTokenAcquisitionResponseParserError,
                 RegisterLinkedUserCredentialsCommand] =
        decodeIdToken(t, clientConfig)
          .toRight(ParseToRegisterCommandError)
          .map(id =>
            RegisterLinkedUserCredentialsCommand(id, clientConfig.serviceName))

      override def parseToSignInCommand(t: LineAccessTokenAcquisitionResponse,
                                        clientConfig: ClientConfig)
        : Either[AccessTokenAcquisitionResponseParserError,
                 SignInWithLinkageCommand] =
        decodeIdToken(t, clientConfig)
          .toRight(ParseToSignInCommandError)
          .map(id => SignInWithLinkageCommand(id, clientConfig.serviceName))
    }
}
