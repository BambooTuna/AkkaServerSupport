package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.{Kleisli, OptionT}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials
import monix.eval.Task

trait LinkedUserCredentialsDao[DBSession] {

  type M[O] = Kleisli[Task, DBSession, O]

  def insert(record: LinkedUserCredentials): M[LinkedUserCredentials]

  def resolveById(id: String): OptionT[M, LinkedUserCredentials]

  def resolveByServiceId(serviceId: String): OptionT[M, LinkedUserCredentials]

  def delete(id: String): OptionT[M, Unit]

}
