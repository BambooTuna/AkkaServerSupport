package com.github.BambooTuna.AkkaServerSupport.authentication.model

import com.github.BambooTuna.AkkaServerSupport.authentication.json.PasswordInitializationRequestJson

trait UserCredentials {
  type Id
  type SigninId
  type SigninPass <: EncryptedPassword

  val id: Id
  val signinId: SigninId
  val signinPass: SigninPass

  def doAuthenticationByPassword(inputPass: Any): Boolean

  def initializeAuthentication(
      json: PasswordInitializationRequestJson[_]): Boolean

  def changePassword(newPlainPassword: SigninPass#ValueType): UserCredentials

  def initPassword(): (UserCredentials, SigninPass#ValueType)

}
