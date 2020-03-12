package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.data.EitherT
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AccountAlreadyExistsError,
  AuthenticationCustomError,
  SignInForbiddenError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.SignInRequestJson
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.serializer.JsonRecodeSerializer
import monix.eval.Task

abstract class AuthenticationUseCase[SignUpRequest,
SignInRequest <: SignInRequestJson, Record <: UserCredentials](
    implicit js: JsonRecodeSerializer[SignUpRequest, Record]) {

  val userCredentialsDao: UserCredentialsDao[Record]

  def signUp(
      json: SignUpRequest): EitherT[Task, AuthenticationCustomError, Record] =
    EitherT {
      userCredentialsDao
        .insert(js.toRecode(json))
        .map(Right(_))
        .onErrorHandle(_ => Left(AccountAlreadyExistsError))
    }

  def signIn(
      json: SignInRequest): EitherT[Task, AuthenticationCustomError, Record] =
    userCredentialsDao
      .resolveBySigninId(json.signInId)
      .filter(_.doAuthenticationByPassword(json.signInPass))
      .toRight[AuthenticationCustomError](SignInForbiddenError)

}
