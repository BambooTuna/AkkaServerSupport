package com.github.BambooTuna.AkkaServerSupport.sample.oauth

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.github.BambooTuna.AkkaServerSupport.authentication.router.error.CustomError
import com.github.BambooTuna.AkkaServerSupport.cooperation.line.{
  LineAccessTokenAcquisitionRequest,
  LineAccessTokenAcquisitionResponseFailed,
  LineAccessTokenAcquisitionResponseSuccess,
  LineClientAuthenticationRequest,
  LineClientAuthenticationResponseFailed,
  LineClientAuthenticationResponseSuccess
}
import com.github.BambooTuna.AkkaServerSupport.cooperation.model.{
  ClientConfig,
  OAuth2Settings
}
import com.github.BambooTuna.AkkaServerSupport.cooperation.useCase.OAuth2UseCase
import com.github.BambooTuna.AkkaServerSupport.sample.SystemSettings
import com.github.BambooTuna.AkkaServerSupport.sample.oauth.LineOAuth2UseCaseImpl.DecodeJWTError

import pdi.jwt.{Jwt, JwtAlgorithm}

class LineOAuth2UseCaseImpl(settings: OAuth2Settings)
    extends OAuth2UseCase(settings) {

  override type ClientAuthenticationRequestType =
    LineClientAuthenticationRequest
  override type ClientAuthenticationResponseSuccessType =
    LineClientAuthenticationResponseSuccess
  override type ClientAuthenticationResponseFailedType =
    LineClientAuthenticationResponseFailed
  override type AccessTokenAcquisitionRequestType =
    LineAccessTokenAcquisitionRequest
  override type AccessTokenAcquisitionResponseSuccessType =
    LineAccessTokenAcquisitionResponseSuccess
  override type AccessTokenAcquisitionResponseFailedType =
    LineAccessTokenAcquisitionResponseFailed

  override def generateClientAuthenticationRequest(
      clientConfig: ClientConfig): LineClientAuthenticationRequest =
    LineClientAuthenticationRequest(
      response_type = "code",
      client_id = clientConfig.clientId,
      redirect_uri = clientConfig.redirectUri.toString(),
      state = SystemSettings.generateId(),
      scope = Some("openid email"),
      nonce = None,
      prompt = None,
      max_age = None,
      ui_locales = None,
      bot_prompt = None
    )

  override def generateAccessTokenAcquisitionRequest(code: String)(
      clientConfig: ClientConfig): LineAccessTokenAcquisitionRequest =
    LineAccessTokenAcquisitionRequest(
      grant_type = "authorization_code",
      code = code,
      redirect_uri = clientConfig.redirectUri.toString(),
      client_id = clientConfig.clientId,
      client_secret = clientConfig.clientSecret
    )

  def decodeIdToken(value: AccessTokenAcquisitionResponseSuccessType)
    : Either[CustomError, String] = {
    value.id_token
      .flatMap(
        a =>
          Jwt
            .decode(a,
                    settings.clientConfig.clientSecret,
                    Seq(JwtAlgorithm.HS256))
            .toOption)
      .flatMap(_.subject)
      .toRight(DecodeJWTError)
  }
}

object LineOAuth2UseCaseImpl {
  case object DecodeJWTError extends CustomError {
    override val statusCode: StatusCode = StatusCodes.BadRequest
    override val message: Option[String] = None
  }
}
