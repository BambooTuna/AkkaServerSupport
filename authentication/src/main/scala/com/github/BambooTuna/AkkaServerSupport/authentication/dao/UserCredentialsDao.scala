package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.{Kleisli, OptionT}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import monix.eval.Task

trait UserCredentialsDao[Record <: UserCredentials] {
  type DBSession
  type M[O] = Kleisli[Task, DBSession, O]

  def insert(record: Record): M[Record]

  def resolveById(id: String): OptionT[M, Record]

  def resolveBySigninId(signinId: String): OptionT[M, Record]

  def updatePassword(
      id: String,
      newEncryptedPassword: Record#SigninPass#ValueType): OptionT[M, Unit]

  def activate(id: String, activated: Boolean): OptionT[M, Unit]

  def delete(id: String): OptionT[M, Unit]

}
