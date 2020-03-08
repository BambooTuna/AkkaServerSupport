package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.Monad
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AuthenticationUseCaseError,
  CantFoundUserError,
  SignInIdOrPassWrongError,
  SignUpInsertError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJson,
  SignInRequestJson
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.serializer.JsonRecodeSerializer

abstract class AuthenticationUseCase[SignUpRequest,
SignInRequest <: SignInRequestJson,
PasswordInitializationRequest <: PasswordInitializationRequestJson,
Record <: UserCredentials](
    implicit js: JsonRecodeSerializer[SignUpRequest, Record]) {

  val userCredentialsDao: UserCredentialsDao[Record]
  type M[O] = userCredentialsDao.M[O]

  def signUp(
      json: SignUpRequest): M[Either[AuthenticationUseCaseError, Record]] =
    userCredentialsDao
      .insert(js.toRecode(json))
      .map(Right(_))
      .mapF(_.onErrorHandle(_ => Left(SignUpInsertError)))

  def signIn(json: SignInRequest)(
      implicit F: Monad[M]): M[Either[AuthenticationUseCaseError, Record]] =
    userCredentialsDao
      .resolveById(json.signInId)
      .filter(_.doAuthenticationByPassword(json.signInPass))
      .toRight[AuthenticationUseCaseError](SignInIdOrPassWrongError)
      .value

  def passwordInitialization(json: PasswordInitializationRequest)(
      implicit F: Monad[M])
    : M[Either[AuthenticationUseCaseError, Record#SigninPass#ValueType]] =
    (for {
      u <- userCredentialsDao
        .resolveById(json.signInId)
      (newCredentials, newPlainPassword) = u.initPassword()
      _ <- userCredentialsDao
        .update(newCredentials.asInstanceOf[Record])
    } yield newPlainPassword)
      .toRight[AuthenticationUseCaseError](CantFoundUserError)
      .value

}
