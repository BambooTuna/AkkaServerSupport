package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import cats.Monad
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJson,
  SignInRequestJson,
  SignUpRequestJson
}
import com.github.BambooTuna.AkkaServerSupport.authentication.router.error.CustomError
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase._

trait AuthenticationUseCase {

  val userCredentialsDao: UserCredentialsDao

  type IO[O] = userCredentialsDao.IO[O]
  type M[O] = userCredentialsDao.M[O]
  type SigninId = userCredentialsDao.Id
  type Record = userCredentialsDao.Record

  type SignUpRequest <: SignUpRequestJson[Record]
  type SignInRequest <: SignInRequestJson[Record]
  type PasswordInitializationRequest <: PasswordInitializationRequestJson[
    Record]

  def ioErrorHandling[T, U >: T](io: IO[T], f: Throwable => U): IO[U]

  def signUp(json: SignUpRequest)(
      implicit F: Monad[IO]): M[Either[AuthenticationUseCaseError, Record]] =
    userCredentialsDao
      .insert(json.createUserCredentials)
      .map(Right(_))
      .mapF(io => ioErrorHandling(io, _ => Left(SignUpInsertError)))

  def signIn(json: SignInRequest)(
      implicit F: Monad[M]): M[Either[AuthenticationUseCaseError, Record]] =
    userCredentialsDao
      .resolveById(json.signInId.asInstanceOf[SigninId])
      .filter(_.doAuthenticationByPassword(json.signInPass))
      .toRight[AuthenticationUseCaseError](SignInIdOrPassWrongError)
      .value

  def passwordInitialization(json: PasswordInitializationRequest)(
      implicit F: Monad[M])
    : M[Either[AuthenticationUseCaseError, Record#SigninPass#ValueType]] =
    (for {
      u <- userCredentialsDao
        .resolveById(json.signInId.asInstanceOf[SigninId])
        .filter(_.initializeAuthentication(json))
      (newCredentials, newPlainPassword) = u.initPassword()
      _ <- userCredentialsDao
        .update(newCredentials.asInstanceOf[Record])
    } yield newPlainPassword)
      .toRight[AuthenticationUseCaseError](CantFoundUserError)
      .value

}

object AuthenticationUseCase {
  sealed trait AuthenticationUseCaseError extends CustomError
  case object SignUpInsertError extends AuthenticationUseCaseError {
    override val statusCode: StatusCode = StatusCodes.BadRequest
    override val message: Option[String] = Some("SignUpInsertError")
  }
  case object SignInIdOrPassWrongError extends AuthenticationUseCaseError {
    override val statusCode: StatusCode = StatusCodes.BadRequest
    override val message: Option[String] = Some("SignInIdOrPassWrongError")
  }
  case object CantFoundUserError extends AuthenticationUseCaseError {
    override val statusCode: StatusCode = StatusCodes.BadRequest
    override val message: Option[String] = Some("CantFoundUserError")
  }
}
