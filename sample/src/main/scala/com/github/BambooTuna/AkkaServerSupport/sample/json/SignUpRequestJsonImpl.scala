package com.github.BambooTuna.AkkaServerSupport.sample.json

import com.github.BambooTuna.AkkaServerSupport.authentication.json.SignUpRequestJson
import com.github.BambooTuna.AkkaServerSupport.sample.SystemSettings
import com.github.BambooTuna.AkkaServerSupport.sample.model.{
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
