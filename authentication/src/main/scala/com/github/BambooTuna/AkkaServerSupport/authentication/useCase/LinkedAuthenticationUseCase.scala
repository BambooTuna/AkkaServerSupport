package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import cats.Monad
import cats.data.{EitherT, Kleisli}
import com.github.BambooTuna.AkkaServerSupport.authentication.command.LinkedSignUpInRequestCommand
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.{
  LinkedUserCredentialsDao,
  UserCredentialsDao
}
import com.github.BambooTuna.AkkaServerSupport.authentication.router.error.CustomError
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase.AuthenticationUseCaseError
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.LinkedAuthenticationUseCase.{
  CooperationFailureError,
  LinkedAuthenticationUseCaseError,
  RegisteredError
}

trait LinkedAuthenticationUseCase {

  val linkedUserCredentialsDao: LinkedUserCredentialsDao

  type IO[O] = linkedUserCredentialsDao.IO[O]
  type M[O] = linkedUserCredentialsDao.M[O]
  type SigninId = linkedUserCredentialsDao.Id
  type Record = linkedUserCredentialsDao.Record

  type LinkedSignUpInRequest <: LinkedSignUpInRequestCommand[Record]

  def ioErrorHandling[T, U >: T](io: IO[T], f: Throwable => U): IO[U]

  def signUp(command: LinkedSignUpInRequest)(implicit F: Monad[IO])
    : M[Either[LinkedAuthenticationUseCaseError, Record]] = {
    linkedUserCredentialsDao
      .insert(command.createLinkedUserCredentials)
      .map(Right(_))
      .mapF(io => ioErrorHandling(io, _ => Left(RegisteredError)))
  }

  def signIn(command: LinkedSignUpInRequest)(implicit F: Monad[M])
    : M[Either[LinkedAuthenticationUseCaseError, Record]] =
    linkedUserCredentialsDao
      .resolveById(command.id.asInstanceOf[SigninId])
      .filter(_.service == command.service)
      .toRight[LinkedAuthenticationUseCaseError](CooperationFailureError)
      .value

}

object LinkedAuthenticationUseCase {
  sealed trait LinkedAuthenticationUseCaseError extends CustomError
  case object RegisteredError extends LinkedAuthenticationUseCaseError {
    override val statusCode: StatusCode = StatusCodes.BadRequest
    override val message: Option[String] = Some("RegisteredError")
  }
  case object CooperationFailureError extends LinkedAuthenticationUseCaseError {
    override val statusCode: StatusCode = StatusCodes.BadRequest
    override val message: Option[String] = Some("CooperationFailureError")
  }
}
