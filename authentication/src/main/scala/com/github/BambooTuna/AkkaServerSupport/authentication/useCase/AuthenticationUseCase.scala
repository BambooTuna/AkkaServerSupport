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
  type Id = userCredentialsDao.Id
  type SignInId = userCredentialsDao.SignInId
  type Record = userCredentialsDao.Record

  type SignUpRequest <: SignUpRequestJson[Record]
  type SignInRequest <: SignInRequestJson[Record]
  type PasswordInitializationRequest <: PasswordInitializationRequestJson[
    Record]

  def signUp(json: SignUpRequest): M[Record] =
    userCredentialsDao.insert(json.createUserCredentials)

  def signIn(json: SignInRequest)(implicit F: Monad[M]): OptionT[M, Record] =
    userCredentialsDao
      .resolveBySignInId(json.signInId.asInstanceOf[SignInId])
      .filter(_.doAuthenticationByPassword(json.signInPass))

  def passwordInitialization(json: PasswordInitializationRequest)(
      implicit F: Monad[M]): OptionT[M, Record#SignInPass#ValueType] =
    for {
      u <- userCredentialsDao
        .resolveBySignInId(json.signInId.asInstanceOf[SignInId])
        .filter(_.initializeAuthentication(json))
      (newCredentials, newPlainPassword) = u.initPassword()
      _ <- OptionT.liftF[M, Record] {
        userCredentialsDao.update(newCredentials.asInstanceOf[Record])
      }
    } yield newPlainPassword

}
