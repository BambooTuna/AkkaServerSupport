package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.Monad
import com.github.BambooTuna.AkkaServerSupport.authentication.command.{
  RegisterLinkedUserCredentialsCommand,
  SignInWithLinkageCommand
}
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.LinkedUserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AccountNotFoundError,
  LinkedAuthenticationUseCaseError,
  RegisteredError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.serializer.JsonRecodeSerializer

abstract class LinkedAuthenticationUseCase(
    implicit rs: JsonRecodeSerializer[RegisterLinkedUserCredentialsCommand,
                                      LinkedUserCredentials]) {

  val linkedUserCredentialsDao: LinkedUserCredentialsDao
  type M[O] = linkedUserCredentialsDao.M[O]

  def register(command: RegisterLinkedUserCredentialsCommand)
    : M[Either[LinkedAuthenticationUseCaseError, LinkedUserCredentials]] = {
    linkedUserCredentialsDao
      .insert(rs.toRecode(command))
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
