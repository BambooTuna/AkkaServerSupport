package com.github.BambooTuna.AkkaServerSupport.sample.model

import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials

case class UserCredentialsImpl(id: String,
                               signinId: String,
                               signinPass: EncryptedPasswordImpl,
                               activated: Boolean)
    extends UserCredentials {
  override type SigninPass = EncryptedPasswordImpl

  override def doAuthenticationByPassword(inputPass: Any): Boolean =
    signinPass == inputPass

}
