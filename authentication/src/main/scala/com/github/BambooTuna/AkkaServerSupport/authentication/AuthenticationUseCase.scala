package com.github.BambooTuna.AkkaServerSupport.authentication

import cats.{Functor, Monad}
import cats.data.{Kleisli, OptionT}
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{PasswordInitializationRequestJson, SignInRequestJson, SignUpRequestJson}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import monix.eval.Task

trait AuthenticationUseCase {
  // Like Future, Task
  type IO[_]
  type DBSession
  type M[O] = Kleisli[IO, DBSession, O]

  type Id
  type U <: UserCredentials

  protected val userCredentialsDao: UserCredentialsDao[M[U], Id, U#SignInId, U]

  def signUp(json: SignUpRequestJson[U]): M[U] =
    userCredentialsDao.insert(json.createUserCredentials)

  def signIn(json: SignInRequestJson[U])(
      implicit F: Functor[M]): OptionT[M, U] =
    userCredentialsDao
      .resolveBySignInId(json.signInId)
      .filter(_.doAuthenticationByPassword(json.signInPass))

  def passwordInitialization(json: PasswordInitializationRequestJson[U])(
      implicit F: Monad[M]): OptionT[M, U#SignInPass] =
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
