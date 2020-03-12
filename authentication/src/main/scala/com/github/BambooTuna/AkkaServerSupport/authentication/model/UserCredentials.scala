package com.github.BambooTuna.AkkaServerSupport.authentication.model

trait UserCredentials {
  type SigninPass <: EncryptedPassword

  val id: String
  val signinId: String
  val signinPass: SigninPass
  val activated: Boolean

  def doAuthenticationByPassword(inputPass: Any): Boolean

  def changePassword(newPlainPassword: SigninPass#ValueType): UserCredentials

  def initPassword(): (UserCredentials, SigninPass#ValueType)

}
