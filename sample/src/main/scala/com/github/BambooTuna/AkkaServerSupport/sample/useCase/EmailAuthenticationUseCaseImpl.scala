package com.github.BambooTuna.AkkaServerSupport.sample.useCase

import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.EmailAuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import com.github.BambooTuna.AkkaServerSupport.sample.dao.UserCredentialsDaoImpl
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl
import monix.eval.Task

class EmailAuthenticationUseCaseImpl(strategy: StorageStrategy[String, String])
    extends EmailAuthenticationUseCase[UserCredentialsImpl](strategy) {
  override val userCredentialsDao: UserCredentialsDaoImpl =
    new UserCredentialsDaoImpl

  override protected def sendActivateCodeTo(mail: String,
                                            code: String): Task[Unit] = {
    println(s"sendActivateCodeEmailTo: $mail | $code")
    Task.unit
  }

  override protected def sendInitializationCodeTo(mail: String,
                                                  code: String): Task[Unit] = {
    println(s"sendInitializationCodeEmailTo: $mail | $code")
    Task.unit
  }

  override protected def sendNewPlainPasswordTo(
      mail: String,
      newPlainPassword: String): Task[Unit] = {
    println(s"sendNewPlainPasswordTo: $mail | $newPlainPassword")
    Task.unit
  }
}
