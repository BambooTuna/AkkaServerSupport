package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AccountAlreadyExistsError,
  AuthenticationCustomError,
  SignInForbiddenError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.SignInRequestJson
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.serializer.JsonRecodeSerializer

abstract class AuthenticationUseCase[SignUpRequest,
SignInRequest <: SignInRequestJson, Record <: UserCredentials](
    implicit js: JsonRecodeSerializer[SignUpRequest, Record]) {

  val userCredentialsDao: UserCredentialsDao[Record]
  type M[O] = userCredentialsDao.M[O]

  def signUp(
      json: SignUpRequest): M[Either[AuthenticationCustomError, Record]] =
    userCredentialsDao
      .insert(js.toRecode(json))
      .map(Right(_))
      .mapF(_.onErrorHandle(_ => Left(AccountAlreadyExistsError)))

  def signIn(
      json: SignInRequest): M[Either[AuthenticationCustomError, Record]] =
    userCredentialsDao
      .resolveBySigninId(json.signInId)
      .filter(_.doAuthenticationByPassword(json.signInPass))
      .toRight[AuthenticationCustomError](SignInForbiddenError)
      .value

}
