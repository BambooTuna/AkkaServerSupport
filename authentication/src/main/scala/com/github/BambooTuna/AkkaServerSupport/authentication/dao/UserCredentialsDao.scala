package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.{Kleisli, OptionT}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import monix.eval.Task

trait UserCredentialsDao[Record <: UserCredentials] {

  def insert(record: Record): Task[Record]

  def resolveById(id: String): OptionT[Task, Record]

  def resolveBySigninId(signinId: String): OptionT[Task, Record]

  def updatePassword(
      id: String,
      newEncryptedPassword: Record#SigninPass#ValueType): OptionT[Task, Unit]

  def activate(id: String, activated: Boolean): OptionT[Task, Unit]

  def delete(id: String): OptionT[Task, Unit]

}
