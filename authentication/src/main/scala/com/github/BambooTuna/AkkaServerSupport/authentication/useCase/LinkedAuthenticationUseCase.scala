package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.Monad
import com.github.BambooTuna.AkkaServerSupport.authentication.command.{RegisterLinkedUserCredentialsCommand, SignInWithLinkageCommand}
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.LinkedUserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.{AccountNotFoundError, LinkedAuthenticationUseCaseError, RegisteredError}

class LinkedAuthenticationUseCase(linkedUserCredentialsDao: LinkedUserCredentialsDao) {

  type M[O] = linkedUserCredentialsDao.M[O]

  def register(command: RegisterLinkedUserCredentialsCommand)
    : M[Either[LinkedAuthenticationUseCaseError, LinkedUserCredentials]] = {
    linkedUserCredentialsDao
      .insert(command.createLinkedUserCredentials)
      .map(Right(_))
      .mapF(_.onErrorHandle(_ => Left(RegisteredError)))
  }

  def signIn(command: SignInWithLinkageCommand)(implicit F: Monad[M])
    : M[Either[LinkedAuthenticationUseCaseError, LinkedUserCredentials]] =
    linkedUserCredentialsDao
      .resolveByServiceId(command.serviceId)
      .filter(_.serviceName == command.serviceName)
      .toRight[LinkedAuthenticationUseCaseError](AccountNotFoundError)
      .value

}
