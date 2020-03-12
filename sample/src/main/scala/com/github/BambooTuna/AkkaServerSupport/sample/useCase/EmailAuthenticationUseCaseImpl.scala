package com.github.BambooTuna.AkkaServerSupport.sample.useCase

import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.EmailAuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl
import monix.eval.Task
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.email.EmailBuilder

class EmailAuthenticationUseCaseImpl(
    val userCredentialsDao: UserCredentialsDao[UserCredentialsImpl],
    val cacheStorage: StorageStrategy[String, String],
    mailer: Mailer,
) extends EmailAuthenticationUseCase[UserCredentialsImpl] {

  override protected def sendActivateCodeTo(mail: String,
                                            code: String): Task[Unit] = {
    val email = EmailBuilder
      .startingBlank()
      .from("bambootuna@gmail.com")
      .to(mail)
      .withSubject("新規ユーザー登録")
      .withPlainText(
        s"新規登録が成功しました！\n以下リンクにアクセスして、アカウントを有効化してください。\nhttp://localhost:8080/activate/$code")
      .buildEmail()
    Task { mailer.sendMail(email) }
  }

  override protected def sendInitializationCodeTo(mail: String,
                                                  code: String): Task[Unit] = {
    val email = EmailBuilder
      .startingBlank()
      .from("bambootuna@gmail.com")
      .to(mail)
      .withSubject("パスワードの初期化")
      .withPlainText(
        s"以下リンクにアクセスして、パスワードを初期化してください。\nhttp://localhost:8080/init/$code")
      .buildEmail()
    Task { mailer.sendMail(email) }
  }

  override protected def sendNewPlainPasswordTo(
      mail: String,
      newPlainPassword: String): Task[Unit] = {
    val email = EmailBuilder
      .startingBlank()
      .from("bambootuna@gmail.com")
      .to(mail)
      .withSubject("新規パスワード通知")
      .withPlainText(
        s"パスワードが初期化されました、新規パスワードは以下のようになっています。\n${newPlainPassword}")
      .buildEmail()
    Task { mailer.sendMail(email) }
  }
}
