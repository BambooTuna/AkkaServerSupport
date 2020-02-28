package com.github.BambooTuna.AkkaServerSupport.authentication.json

import com.github.BambooTuna.AkkaServerSupport.authentication.SystemSettings
import com.github.BambooTuna.AkkaServerSupport.authentication.model.{
  EncryptedPasswordImpl,
  UserCredentialsImpl
}

case class SignUpRequestJsonImpl(mail: String, pass: String)
    extends SignUpRequestJson[UserCredentialsImpl] {
  override def createUserCredentials: UserCredentialsImpl =
    UserCredentialsImpl(id = SystemSettings.generateId(),
                        signinId = mail,
                        signinPass =
                          EncryptedPasswordImpl(pass).changeEncryptedPass(pass))
}
