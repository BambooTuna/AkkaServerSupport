package com.github.BambooTuna.AkkaServerSupport.sample.controller

import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.controller.AuthenticationController
import com.github.BambooTuna.AkkaServerSupport.authentication.session.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.EmailAuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  Session,
  StorageStrategy
}
import com.github.BambooTuna.AkkaServerSupport.sample.json.{
  PasswordInitializationRequestJsonImpl,
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl
import com.github.BambooTuna.AkkaServerSupport.sample.useCase.{
  AuthenticationUseCaseImpl,
  EmailAuthenticationUseCaseImpl
}
import doobie.hikari.HikariTransactor
import io.circe.generic.auto._
import monix.eval.Task

class AuthenticationControllerImpl(
    val dbSession: Resource[Task, HikariTransactor[Task]],
    strategy: StorageStrategy[String, String])(
    implicit session: Session[String, SessionToken])
    extends AuthenticationController[Resource[Task, HikariTransactor[Task]],
                                     SignUpRequestJsonImpl,
                                     SignInRequestJsonImpl,
                                     PasswordInitializationRequestJsonImpl,
                                     UserCredentialsImpl] {
  override val authenticationUseCase: AuthenticationUseCaseImpl =
    new AuthenticationUseCaseImpl
  override val emailAuthenticationUseCase: EmailAuthenticationUseCaseImpl =
    new EmailAuthenticationUseCaseImpl(strategy)
}
