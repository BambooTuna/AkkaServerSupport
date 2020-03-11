package com.github.BambooTuna.AkkaServerSupport.sample.useCase

import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.LinkedAuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.sample.dao.LinkedUserCredentialsDaoImpl
import doobie.hikari.HikariTransactor
import monix.eval.Task

class LinkedAuthenticationUseCaseImpl
    extends LinkedAuthenticationUseCase[Resource[Task, HikariTransactor[Task]]] {
  override val linkedUserCredentialsDao: LinkedUserCredentialsDaoImpl =
    new LinkedUserCredentialsDaoImpl
}
