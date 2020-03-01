package com.github.BambooTuna.AkkaServerSupport.sample.json

import com.github.BambooTuna.AkkaServerSupport.authentication.json.PasswordInitializationRequestJson
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl

case class PasswordInitializationRequestJsonImpl(mail: String)
    extends PasswordInitializationRequestJson[UserCredentialsImpl] {
  override val signInId: String = mail
}
