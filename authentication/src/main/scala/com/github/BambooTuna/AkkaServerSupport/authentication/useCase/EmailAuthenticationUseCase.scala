package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.data.EitherT
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AccountNotFoundError,
  ActivateAccountError,
  AlreadyActivatedError,
  AuthenticationCustomError,
  InvalidActivateCodeError,
  InvalidInitializationCodeError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import monix.eval.Task

trait EmailAuthenticationUseCase[Record <: UserCredentials] {

  val cacheStorage: StorageStrategy[String, String]
  val userCredentialsDao: UserCredentialsDao[Record]

  protected def generateCode: String =
    java.util.UUID.randomUUID.toString.replaceAll("-", "")

  protected def sendActivateCodeTo(mail: String, code: String): Task[Unit]
  protected def sendInitializationCodeTo(mail: String, code: String): Task[Unit]
  protected def sendNewPlainPasswordTo(mail: String,
                                       newPlainPassword: String): Task[Unit]

  protected def findActivateCode(code: String): Task[Option[String]] =
    for {
      r <- Task.fromFuture(cacheStorage.find(code))
      _ <- Task.fromFuture(cacheStorage.remove(code))
    } yield r

  def issueActivateCode(
      userId: String): EitherT[Task, AuthenticationCustomError, Unit] = {
    (for {
      record <- userCredentialsDao
        .resolveById(userId)
        .toRight(AccountNotFoundError)
        .flatMap(
          a =>
            EitherT.fromEither(
              Either.cond[AuthenticationCustomError, Record](
                !a.activated,
                a,
                AlreadyActivatedError)))
      _ <- EitherT[Task, AuthenticationCustomError, Unit] {
        for {
          code <- Task.pure(generateCode)
          _ <- Task.fromFuture(cacheStorage.store(code, userId))
          _ <- sendActivateCodeTo(record.signinId, code)
        } yield Right()
      }
    } yield ())
  }

  def activateAccount(
      code: String): EitherT[Task, AuthenticationCustomError, Unit] =
    (for {
      id <- EitherT[Task, AuthenticationCustomError, String] {
        findActivateCode(code).map(_.toRight(InvalidActivateCodeError))
      }
      _ <- userCredentialsDao
        .activate(id, activated = true)
        .toRight[AuthenticationCustomError](ActivateAccountError)
    } yield ())

  def issueInitializationCode(
      mail: String): EitherT[Task, AuthenticationCustomError, Unit] = {
    (for {
      recode <- userCredentialsDao
        .resolveBySigninId(mail)
        .toRight(AccountNotFoundError)
      code = generateCode
      _ <- EitherT[Task, AuthenticationCustomError, Unit] {
        (for {
          _ <- Task.fromFuture(cacheStorage.store(code, recode.id))
          _ <- sendInitializationCodeTo(mail, code)
        } yield ()).map(_ => Right())
      }
    } yield ())
  }

  def initAccountPassword(
      code: String): EitherT[Task, AuthenticationCustomError, Unit] =
    (for {
      id <- EitherT[Task, AuthenticationCustomError, String] {
        findActivateCode(code).map(_.toRight(InvalidInitializationCodeError))
      }
      record <- userCredentialsDao
        .resolveById(id)
        .toRight[AuthenticationCustomError](AccountNotFoundError)
      (newRecord, newPlainPassword) = record.initPassword()
      _ <- userCredentialsDao
        .updatePassword(id, newRecord.signinPass.encryptedPass)
        .toRight[AuthenticationCustomError](ActivateAccountError)
      _ <- EitherT[Task, AuthenticationCustomError, Unit] {
        sendNewPlainPasswordTo(newRecord.signinId, newPlainPassword).map(_ =>
          Right())
      }
    } yield ())

}
