package com.github.BambooTuna.AkkaServerSupport.authentication.model

trait EncryptedPassword {
  type ValueType = String

  val encryptedPass: ValueType

  override def equals(obj: Any): Boolean = obj match {
    case v: ValueType => encryptedPass == encryption(v)
    case _            => false
  }

  protected def encryption(plainPass: ValueType): String

  def changeEncryptedPass(plainPass: ValueType): EncryptedPassword

}
