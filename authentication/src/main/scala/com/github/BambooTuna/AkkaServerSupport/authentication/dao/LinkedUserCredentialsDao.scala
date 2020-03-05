package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.{Kleisli, OptionT}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials

trait LinkedUserCredentialsDao {
  type IO[_]
  type DBSession

  type M[O] = Kleisli[IO, DBSession, O]
  type Record <: LinkedUserCredentials

  type Id
  type ServiceId

  def insert(record: Record): M[Record]

  def resolveById(id: Id): OptionT[M, Record]

  def resolveByServiceId(serviceId: ServiceId): OptionT[M, Record]

  def delete(id: Id): OptionT[M, Id]

}
