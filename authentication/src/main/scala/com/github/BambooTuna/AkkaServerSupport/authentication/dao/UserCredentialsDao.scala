package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.OptionT

trait UserCredentialsDao[M[_], Id, SignInId, Record] {

  def insert(record: Record): M[Record]

  def resolveById(id: Id): OptionT[M, Record]

  def resolveBySignInId(id: SignInId): OptionT[M, Record]

  def update(record: Record): M[Record]

  def delete(id: Id): M[Id]

}
