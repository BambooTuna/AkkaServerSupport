package com.github.BambooTuna.AkkaServerSupport.sample.useCase

import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.sample.dao.UserCredentialsDaoImpl
import com.github.BambooTuna.AkkaServerSupport.sample.json.{
  PasswordInitializationRequestJsonImpl,
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl

class AuthenticationUseCaseImpl
    extends AuthenticationUseCase[SignUpRequestJsonImpl,
                                  SignInRequestJsonImpl,
                                  PasswordInitializationRequestJsonImpl,
                                  UserCredentialsImpl] {
  override val userCredentialsDao: UserCredentialsDaoImpl =
    new UserCredentialsDaoImpl
}
