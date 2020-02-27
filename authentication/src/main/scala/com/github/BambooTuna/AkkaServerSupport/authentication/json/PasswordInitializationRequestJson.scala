package com.github.BambooTuna.AkkaServerSupport.authentication.json

import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials

trait PasswordInitializationRequestJson[U <: UserCredentials] {
  val signInId: U#SignInId
}
