package com.github.BambooTuna.AkkaServerSupport.authentication.json

import com.github.BambooTuna.AkkaServerSupport.authentication.model.{
  EncryptedPasswordImpl,
  UserCredentialsImpl
}

case class SignInRequestJsonImpl(mail: String, pass: String)
    extends SignInRequestJson[UserCredentialsImpl] {
  override val signInId: String = mail
  override val signInPass: String = pass
}
