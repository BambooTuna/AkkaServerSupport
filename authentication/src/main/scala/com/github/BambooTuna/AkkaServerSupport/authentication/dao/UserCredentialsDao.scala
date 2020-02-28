package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.{Kleisli, OptionT}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials

trait UserCredentialsDao {
  type IO[_]
  type DBSession

  type M[O] = Kleisli[IO, DBSession, O]
  type Record <: UserCredentials

  type Id

  def insert(record: Record): OptionT[M, Record]

  def resolveById(id: Id): OptionT[M, Record]

  def update(record: Record): OptionT[M, Record]

  def delete(id: Id): OptionT[M, Id]

}
