package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.Monad
import cats.data.EitherT
import com.github.BambooTuna.AkkaServerSupport.authentication.command.{
  RegisterLinkedUserCredentialsCommand,
  SignInWithLinkageCommand
}
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.LinkedUserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AccountNotFoundError,
  OAuth2CustomError,
  AccountAlreadyExistsError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.serializer.JsonRecodeSerializer

abstract class LinkedAuthenticationUseCase(
    implicit rs: JsonRecodeSerializer[RegisterLinkedUserCredentialsCommand,
                                      LinkedUserCredentials]) {

  val linkedUserCredentialsDao: LinkedUserCredentialsDao
  type M[O] = linkedUserCredentialsDao.M[O]

  def register(command: RegisterLinkedUserCredentialsCommand)
    : EitherT[M, OAuth2CustomError, LinkedUserCredentials] = {
    EitherT[M, OAuth2CustomError, LinkedUserCredentials] {
      linkedUserCredentialsDao
        .insert(rs.toRecode(command))
        .map(Right(_))
        .mapF(_.onErrorHandle(_ => Left(AccountAlreadyExistsError)))
    }
  }

  def signIn(command: SignInWithLinkageCommand)(implicit F: Monad[M])
    : EitherT[M, OAuth2CustomError, LinkedUserCredentials] =
    linkedUserCredentialsDao
      .resolveByServiceId(command.serviceId)
      .filter(_.serviceName == command.serviceName)
      .toRight[OAuth2CustomError](AccountNotFoundError)

}
