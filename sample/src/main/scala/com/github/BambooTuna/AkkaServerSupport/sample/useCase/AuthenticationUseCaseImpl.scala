package com.github.BambooTuna.AkkaServerSupport.sample.useCase

import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.sample.dao.UserCredentialsDaoImpl
import com.github.BambooTuna.AkkaServerSupport.sample.json.{
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl

class AuthenticationUseCaseImpl
    extends AuthenticationUseCase[SignUpRequestJsonImpl,
                                  SignInRequestJsonImpl,
                                  UserCredentialsImpl] {
  override val userCredentialsDao: UserCredentialsDaoImpl =
    new UserCredentialsDaoImpl
}
