package com.github.BambooTuna.AkkaServerSupport.authentication.model

import com.github.BambooTuna.AkkaServerSupport.authentication.json.PasswordInitializationRequestJson

case class UserCredentialsImpl(signInId: String,
                               signInPass: EncryptedPasswordImpl)
    extends UserCredentials {
  override type SignInId = String
  override type SignInPass = EncryptedPasswordImpl

  override def doAuthenticationByPassword(inputPass: Any): Boolean =
    signInPass == inputPass

  //TODO
  override def initializeAuthentication(
      json: PasswordInitializationRequestJson[_]): Boolean = true

  override def changePassword(newPlainPassword: String): UserCredentialsImpl =
    copy(signInPass = signInPass.changeEncryptedPass(newPlainPassword))

  override def initPassword(): (UserCredentialsImpl, String) = {
    //TODO
    val newPlainPassword = "admin"
    (changePassword(newPlainPassword), newPlainPassword)
  }

}
