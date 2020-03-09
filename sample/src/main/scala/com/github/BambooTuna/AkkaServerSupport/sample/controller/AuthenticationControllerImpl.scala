package com.github.BambooTuna.AkkaServerSupport.sample.controller

import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.controller.AuthenticationController
import com.github.BambooTuna.AkkaServerSupport.authentication.session.SessionToken
import com.github.BambooTuna.AkkaServerSupport.core.session.Session
import com.github.BambooTuna.AkkaServerSupport.sample.json.{
  PasswordInitializationRequestJsonImpl,
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl
import com.github.BambooTuna.AkkaServerSupport.sample.useCase.AuthenticationUseCaseImpl
import doobie.hikari.HikariTransactor
import io.circe.generic.auto._
import monix.eval.Task

class AuthenticationControllerImpl(
    val dbSession: Resource[Task, HikariTransactor[Task]])(
    implicit session: Session[String, SessionToken])
    extends AuthenticationController[SignUpRequestJsonImpl,
                                     SignInRequestJsonImpl,
                                     PasswordInitializationRequestJsonImpl,
                                     UserCredentialsImpl] {
  override val authenticationUseCase: AuthenticationUseCaseImpl =
    new AuthenticationUseCaseImpl
}
