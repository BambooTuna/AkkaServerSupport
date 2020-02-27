package com.github.BambooTuna.AkkaServerSupport.authentication.json

import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentialsImpl

case class PasswordInitializationRequestJsonImpl(signInId: String)
    extends PasswordInitializationRequestJson[UserCredentialsImpl]
