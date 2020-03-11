package com.github.BambooTuna.AkkaServerSupport.authentication.error

import akka.http.javadsl.server.CustomRejection

sealed trait OAuth2CustomError extends CustomRejection
case object ParseParameterFailedError extends OAuth2CustomError

case object ParseAccessTokenAcquisitionResponseError extends OAuth2CustomError
case object CSRFTokenForbiddenError extends OAuth2CustomError

case object LinkedAccountAlreadyExistsError extends OAuth2CustomError
case object LinkedAccountNotFoundError extends OAuth2CustomError

case class ParseToRegisterCommandError(message: String)
    extends OAuth2CustomError
case class ParseToSignInCommandError(message: String) extends OAuth2CustomError
