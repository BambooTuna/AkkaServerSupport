package com.github.BambooTuna.AkkaServerSupport.authentication.json

import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentialsImpl

case class PasswordInitializationRequestJsonImpl(mail: String)
    extends PasswordInitializationRequestJson[UserCredentialsImpl] {
  override val signInId: String = mail
}
