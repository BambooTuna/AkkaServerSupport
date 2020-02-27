package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.{Kleisli, OptionT}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials

trait UserCredentialsDao {
  type IO[_]
  type DBSession

  type M[O] = Kleisli[IO, DBSession, O]
  type Id
  type SignInId
  type Record <: UserCredentials

  def insert(record: Record): M[Record]

  def resolveById(id: Id): OptionT[M, Record]

  def resolveBySignInId(id: SignInId): OptionT[M, Record]

  def update(record: Record): M[Record]

  def delete(id: Id): M[Id]

}
