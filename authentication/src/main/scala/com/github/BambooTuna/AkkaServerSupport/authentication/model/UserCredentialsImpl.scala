package com.github.BambooTuna.AkkaServerSupport.authentication.model

import com.github.BambooTuna.AkkaServerSupport.authentication.SystemSettings
import com.github.BambooTuna.AkkaServerSupport.authentication.json.PasswordInitializationRequestJson

case class UserCredentialsImpl(id: String,
                               signInId: String,
                               signInPass: EncryptedPasswordImpl)
    extends UserCredentials {
  override type Id = String
  override type SignInId = String
  override type SignInPass = EncryptedPasswordImpl

  override def doAuthenticationByPassword(inputPass: Any): Boolean =
    signInPass == inputPass

  //TODO
  override def initializeAuthentication(
      json: PasswordInitializationRequestJson[_]): Boolean = false

  override def changePassword(newPlainPassword: String): UserCredentialsImpl =
    copy(signInPass = signInPass.changeEncryptedPass(newPlainPassword))

  override def initPassword(): (UserCredentialsImpl, String) = {
    val newPlainPassword = SystemSettings.generateId()
    (changePassword(newPlainPassword), newPlainPassword)
  }

}
