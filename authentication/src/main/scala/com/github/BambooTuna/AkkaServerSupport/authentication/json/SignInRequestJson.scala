package com.github.BambooTuna.AkkaServerSupport.authentication.json

trait SignInRequestJson {
  val signInId: String
  val signInPass: String
}
