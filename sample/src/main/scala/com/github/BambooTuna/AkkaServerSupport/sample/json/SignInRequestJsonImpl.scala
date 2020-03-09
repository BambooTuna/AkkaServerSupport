package com.github.BambooTuna.AkkaServerSupport.sample.json

import com.github.BambooTuna.AkkaServerSupport.authentication.json.SignInRequestJson
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl

case class SignInRequestJsonImpl(mail: String, pass: String)
    extends SignInRequestJson {
  override val signInId: String = mail
  override val signInPass: String = pass
}
