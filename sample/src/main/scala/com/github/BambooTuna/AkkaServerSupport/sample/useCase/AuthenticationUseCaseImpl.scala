package com.github.BambooTuna.AkkaServerSupport.sample.useCase

import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.sample.json.{
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl

class AuthenticationUseCaseImpl(
    val userCredentialsDao: UserCredentialsDao[UserCredentialsImpl])
    extends AuthenticationUseCase[SignUpRequestJsonImpl,
                                  SignInRequestJsonImpl,
                                  UserCredentialsImpl]
