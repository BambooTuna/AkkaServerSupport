package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.{Kleisli, OptionT}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials
import monix.eval.Task

trait LinkedUserCredentialsDao {

  def insert(record: LinkedUserCredentials): Task[LinkedUserCredentials]

  def resolveById(id: String): OptionT[Task, LinkedUserCredentials]

  def resolveByServiceId(
      serviceId: String): OptionT[Task, LinkedUserCredentials]

  def delete(id: String): OptionT[Task, Unit]

}
