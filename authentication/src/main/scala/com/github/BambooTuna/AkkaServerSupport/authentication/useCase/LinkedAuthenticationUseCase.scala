package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.data.EitherT
import com.github.BambooTuna.AkkaServerSupport.authentication.command.{
  RegisterLinkedUserCredentialsCommand,
  SignInWithLinkageCommand
}
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.LinkedUserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  LinkedAccountAlreadyExistsError,
  LinkedAccountNotFoundError,
  OAuth2CustomError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.serializer.JsonRecodeSerializer
import monix.eval.Task

abstract class LinkedAuthenticationUseCase(
    implicit rs: JsonRecodeSerializer[RegisterLinkedUserCredentialsCommand,
                                      LinkedUserCredentials]) {

  val linkedUserCredentialsDao: LinkedUserCredentialsDao

  def register(command: RegisterLinkedUserCredentialsCommand)
    : EitherT[Task, OAuth2CustomError, LinkedUserCredentials] = {
    EitherT[Task, OAuth2CustomError, LinkedUserCredentials] {
      linkedUserCredentialsDao
        .insert(rs.toRecode(command))
        .map(Right(_))
        .onErrorHandle(_ => Left(LinkedAccountAlreadyExistsError))
    }
  }

  def signIn(command: SignInWithLinkageCommand)
    : EitherT[Task, OAuth2CustomError, LinkedUserCredentials] =
    linkedUserCredentialsDao
      .resolveByServiceId(command.serviceId)
      .filter(_.serviceName == command.serviceName)
      .toRight[OAuth2CustomError](LinkedAccountNotFoundError)

}
