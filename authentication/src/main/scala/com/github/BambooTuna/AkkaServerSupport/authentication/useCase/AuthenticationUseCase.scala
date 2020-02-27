package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.data.{Kleisli, OptionT}
import cats.{Functor, Monad}
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJson,
  SignInRequestJson,
  SignUpRequestJson
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials

trait AuthenticationUseCase {
  // Like Future, Task
  type IO[_]
  type DBSession
  type M[O] = Kleisli[IO, DBSession, O]

  type Id
  type U <: UserCredentials

  type SignUpRequest <: SignUpRequestJson[U]
  type SignInRequest <: SignInRequestJson[U]
  type PasswordInitializationRequest <: PasswordInitializationRequestJson[U]

  protected val userCredentialsDao: UserCredentialsDao[M, Id, U#SignInId, U]

  def signUp(json: SignUpRequest): M[U] =
    userCredentialsDao.insert(json.createUserCredentials)

  def signIn(json: SignInRequest)(implicit F: Monad[M]): OptionT[M, U] =
    userCredentialsDao
      .resolveBySignInId(json.signInId)
      .filter(_.doAuthenticationByPassword(json.signInPass))

  def passwordInitialization(json: PasswordInitializationRequest)(
      implicit F: Monad[M]): OptionT[M, U#SignInPass#ValueType] =
    for {
      u <- userCredentialsDao
        .resolveBySignInId(json.signInId)
        .filter(_.initializeAuthentication(json))
      (newCredentials, newPlainPassword) = u.initPassword
      _ <- OptionT.liftF[M, U] {
        userCredentialsDao.update(newCredentials.asInstanceOf[U])
      }
    } yield newPlainPassword

}
