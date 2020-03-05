package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import cats.Monad
import com.github.BambooTuna.AkkaServerSupport.authentication.command.{
  LinkedSignInRequestCommand,
  LinkedSignUpRequestCommand
}
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.LinkedUserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.router.error.CustomError
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.LinkedAuthenticationUseCase.{
  CooperationFailureError,
  LinkedAuthenticationUseCaseError,
  RegisteredError
}

trait LinkedAuthenticationUseCase {

  val linkedUserCredentialsDao: LinkedUserCredentialsDao

  type IO[O] = linkedUserCredentialsDao.IO[O]
  type M[O] = linkedUserCredentialsDao.M[O]

  type Id = linkedUserCredentialsDao.Id
  type ServiceId = linkedUserCredentialsDao.ServiceId

  type Record = linkedUserCredentialsDao.Record

  type LinkedSignUpRequest <: LinkedSignUpRequestCommand[Record]
  type LinkedSignInRequest <: LinkedSignInRequestCommand[Record]

  def ioErrorHandling[T, U >: T](io: IO[T], f: Throwable => U): IO[U]

  def signUp(command: LinkedSignUpRequest)(implicit F: Monad[IO])
    : M[Either[LinkedAuthenticationUseCaseError, Record]] = {
    linkedUserCredentialsDao
      .insert(command.createLinkedUserCredentials)
      .map(Right(_))
      .mapF(io => ioErrorHandling(io, _ => Left(RegisteredError)))
  }

  def signIn(command: LinkedSignInRequest)(implicit F: Monad[M])
    : M[Either[LinkedAuthenticationUseCaseError, Record]] =
    linkedUserCredentialsDao
      .resolveByServiceId(command.serviceId.asInstanceOf[ServiceId])
      .filter(_.serviceName == command.serviceName)
      .toRight[LinkedAuthenticationUseCaseError](CooperationFailureError)
      .value

}

object LinkedAuthenticationUseCase {
  sealed trait LinkedAuthenticationUseCaseError extends CustomError
  case object RegisteredError extends LinkedAuthenticationUseCaseError {
    override val statusCode: StatusCode = StatusCodes.TemporaryRedirect
    override val message: Option[String] = Some("連携済")
  }
  case object CooperationFailureError extends LinkedAuthenticationUseCaseError {
    override val statusCode: StatusCode = StatusCodes.BadRequest
    override val message: Option[String] = Some("CooperationFailureError")
  }
}
