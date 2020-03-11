package com.github.BambooTuna.AkkaServerSupport.sample.useCase

import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.sample.dao.UserCredentialsDaoImpl
import com.github.BambooTuna.AkkaServerSupport.sample.json.{
  PasswordInitializationRequestJsonImpl,
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl
import doobie.hikari.HikariTransactor
import monix.eval.Task

class AuthenticationUseCaseImpl
    extends AuthenticationUseCase[Resource[Task, HikariTransactor[Task]],
                                  SignUpRequestJsonImpl,
                                  SignInRequestJsonImpl,
                                  PasswordInitializationRequestJsonImpl,
                                  UserCredentialsImpl] {
  override val userCredentialsDao: UserCredentialsDaoImpl =
    new UserCredentialsDaoImpl
}
