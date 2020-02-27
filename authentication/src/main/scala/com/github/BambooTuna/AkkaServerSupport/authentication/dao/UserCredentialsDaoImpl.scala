package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.OptionT

class UserCredentialsDaoImpl[M[_], Id, SignInId, Record]
    extends UserCredentialsDao[M, Id, SignInId, Record] {
  override def insert(record: Record): M[Record] = ???

  override def resolveById(id: Id): OptionT[M, Record] = ???

  override def resolveBySignInId(id: SignInId): OptionT[M, Record] = ???

  override def update(record: Record): M[Record] = ???

  override def delete(id: Id): M[Id] = ???
}
