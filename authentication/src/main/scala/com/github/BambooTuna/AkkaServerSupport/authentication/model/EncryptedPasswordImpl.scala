package com.github.BambooTuna.AkkaServerSupport.authentication.model

case class EncryptedPasswordImpl(encryptedPass: String)
    extends EncryptedPassword {

  //TODO
  override protected def encryption(plainPass: ValueType): ValueType = plainPass

  override def changeEncryptedPass(
      plainPass: ValueType): EncryptedPasswordImpl =
    copy(encryptedPass = encryption(plainPass))

}
