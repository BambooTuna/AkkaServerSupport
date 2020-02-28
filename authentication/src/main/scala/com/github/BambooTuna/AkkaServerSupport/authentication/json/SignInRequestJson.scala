package com.github.BambooTuna.AkkaServerSupport.authentication.json

import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials

trait SignInRequestJson[U <: UserCredentials] {
  val signInId: U#SigninId
  val signInPass: U#SigninPass#ValueType
}
