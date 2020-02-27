package com.github.BambooTuna.AkkaServerSupport.authentication.json

import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentialsImpl

case class SignInRequestJsonImpl(signInId: String, signInPass: String)
    extends SignInRequestJson[UserCredentialsImpl]
