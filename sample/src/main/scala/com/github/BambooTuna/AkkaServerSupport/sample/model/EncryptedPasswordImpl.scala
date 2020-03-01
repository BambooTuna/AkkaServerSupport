package com.github.BambooTuna.AkkaServerSupport.sample.model

import java.math.BigInteger
import java.security.MessageDigest

import com.github.BambooTuna.AkkaServerSupport.authentication.model.EncryptedPassword

case class EncryptedPasswordImpl(encryptedPass: String)
    extends EncryptedPassword
    with io.getquill.Embedded {

  private val SHA256 = MessageDigest.getInstance("SHA-256")

  override protected def encryption(plainPass: String): String =
    String.format("%064x",
                  new BigInteger(1, SHA256.digest(plainPass.getBytes("UTF-8"))))

  override def changeEncryptedPass(plainPass: String): EncryptedPasswordImpl =
    copy(encryptedPass = encryption(plainPass))

}
