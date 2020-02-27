package com.github.BambooTuna.AkkaServerSupport.authentication.model

import com.github.BambooTuna.AkkaServerSupport.authentication.json.PasswordInitializationRequestJson

trait UserCredentials {
  type SignInId
  type SignInPass <: EncryptedPassword

  val signInId: SignInId
  val signInPass: SignInPass

  def doAuthenticationByPassword(inputPass: Any): Boolean

  def initializeAuthentication(json: PasswordInitializationRequestJson[_]): Boolean

  def changePassword(newPlainPassword: SignInPass#ValueType): UserCredentials

  def initPassword(): (UserCredentials, SignInPass)

}
