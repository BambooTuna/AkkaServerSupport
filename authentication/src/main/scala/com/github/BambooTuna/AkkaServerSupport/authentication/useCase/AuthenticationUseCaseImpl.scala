package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.data.OptionT
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.{
  UserCredentialsDao,
  UserCredentialsDaoImpl
}
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJson,
  PasswordInitializationRequestJsonImpl,
  SignInRequestJson,
  SignInRequestJsonImpl,
  SignUpRequestJson,
  SignUpRequestJsonImpl
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentialsImpl

class AuthenticationUseCaseImpl[IO, DBSession] extends AuthenticationUseCase {
  override type Id = String
  override type U = UserCredentialsImpl
  override type SignUpRequest = SignUpRequestJsonImpl
  override type SignInRequest = SignInRequestJsonImpl
  override type PasswordInitializationRequest =
    PasswordInitializationRequestJsonImpl

  override protected val userCredentialsDao
    : UserCredentialsDao[M, Id, U#SignInId, U] =
    new UserCredentialsDao[M, Id, U#SignInId, U] {
      override def insert(record: U): M[U] = ???
      override def resolveById(id: Id): OptionT[M, U] = ???
      override def resolveBySignInId(id: U#SignInId): OptionT[M, U] = ???
      override def update(record: U): M[U] = ???
      override def delete(id: Id): M[Id] = ???
    }

}
