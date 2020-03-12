package com.github.BambooTuna.AkkaServerSupport.sample.controller

import com.github.BambooTuna.AkkaServerSupport.authentication.controller.AuthenticationController
import com.github.BambooTuna.AkkaServerSupport.authentication.session.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.{
  AuthenticationUseCase,
  EmailAuthenticationUseCase
}
import com.github.BambooTuna.AkkaServerSupport.core.session.Session
import com.github.BambooTuna.AkkaServerSupport.sample.json.{
  PasswordInitializationRequestJsonImpl,
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl
import io.circe.generic.auto._

class AuthenticationControllerImpl(
    val authenticationUseCase: AuthenticationUseCase[SignUpRequestJsonImpl,
                                                     SignInRequestJsonImpl,
                                                     UserCredentialsImpl],
    val emailAuthenticationUseCase: EmailAuthenticationUseCase[
      UserCredentialsImpl]
)(implicit session: Session[String, SessionToken])
    extends AuthenticationController[SignUpRequestJsonImpl,
                                     SignInRequestJsonImpl,
                                     PasswordInitializationRequestJsonImpl,
                                     UserCredentialsImpl]
