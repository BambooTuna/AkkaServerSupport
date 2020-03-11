package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.Monad
import cats.data.{EitherT, Kleisli}
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AccountAlreadyExistsError,
  AccountNotFoundError,
  ActivateAccountError,
  AuthenticationCustomError,
  InvalidActivateCodeError,
  InvalidInitializationCodeError,
  SignInForbiddenError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJson,
  SignInRequestJson
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.serializer.JsonRecodeSerializer
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import monix.eval.Task

abstract class AuthenticationUseCase[DBSession, SignUpRequest,
SignInRequest <: SignInRequestJson,
PasswordInitializationRequest <: PasswordInitializationRequestJson,
Record <: UserCredentials](
    implicit js: JsonRecodeSerializer[SignUpRequest, Record]) {

  val userCredentialsDao: UserCredentialsDao[DBSession, Record]
  type M[O] = userCredentialsDao.M[O]

  def signUp(
      json: SignUpRequest): M[Either[AuthenticationCustomError, Record]] =
    userCredentialsDao
      .insert(js.toRecode(json))
      .map(Right(_))
      .mapF(_.onErrorHandle(_ => Left(AccountAlreadyExistsError)))

  def signIn(json: SignInRequest)(
      implicit F: Monad[M]): M[Either[AuthenticationCustomError, Record]] =
    userCredentialsDao
      .resolveBySigninId(json.signInId)
      .filter(_.doAuthenticationByPassword(json.signInPass))
      .toRight[AuthenticationCustomError](SignInForbiddenError)
      .value

}
