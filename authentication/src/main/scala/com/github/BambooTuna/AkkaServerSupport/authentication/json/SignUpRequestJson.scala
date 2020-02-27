package com.github.BambooTuna.AkkaServerSupport.authentication.json

import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials

trait SignUpRequestJson[U <: UserCredentials] {
  def createUserCredentials: U
}
