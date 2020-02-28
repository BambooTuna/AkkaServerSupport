package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.data.OptionT
import cats.Monad
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJson,
  SignInRequestJson,
  SignUpRequestJson
}

trait AuthenticationUseCase {

  val userCredentialsDao: UserCredentialsDao

  type M[O] = userCredentialsDao.M[O]
  type SigninId = userCredentialsDao.Id
  type Record = userCredentialsDao.Record

  type SignUpRequest <: SignUpRequestJson[Record]
  type SignInRequest <: SignInRequestJson[Record]
  type PasswordInitializationRequest <: PasswordInitializationRequestJson[
    Record]

  def signUp(json: SignUpRequest): OptionT[M, Record] =
    userCredentialsDao.insert(json.createUserCredentials)

  def signIn(json: SignInRequest)(implicit F: Monad[M]): OptionT[M, Record] =
    userCredentialsDao
      .resolveById(json.signInId.asInstanceOf[SigninId])
      .filter(_.doAuthenticationByPassword(json.signInPass))

  def passwordInitialization(json: PasswordInitializationRequest)(
      implicit F: Monad[M]): OptionT[M, Record#SigninPass#ValueType] =
    for {
      u <- userCredentialsDao
        .resolveById(json.signInId.asInstanceOf[SigninId])
        .filter(_.initializeAuthentication(json))
      (newCredentials, newPlainPassword) = u.initPassword()
      _ <- userCredentialsDao
        .update(newCredentials.asInstanceOf[Record])
    } yield newPlainPassword

}
