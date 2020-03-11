package com.github.BambooTuna.AkkaServerSupport.authentication.useCase

import cats.data.{EitherT, Kleisli}
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AccountNotFoundError,
  ActivateAccountError,
  AuthenticationCustomError,
  InvalidActivateCodeError,
  InvalidInitializationCodeError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import monix.eval.Task

abstract class EmailAuthenticationUseCase[DBSession, Record <: UserCredentials](
    strategy: StorageStrategy[String, String]) {

  val userCredentialsDao: UserCredentialsDao[DBSession, Record]
  type M[O] = userCredentialsDao.M[O]

  protected def generateCode: String =
    java.util.UUID.randomUUID.toString.replaceAll("-", "")

  protected def sendActivateCodeEmailTo(mail: String, code: String): Task[Unit]
  protected def sendInitializationCodeEmailTo(mail: String,
                                              code: String): Task[Unit]

  protected def findActivateCode(code: String): Task[Option[String]] =
    for {
      r <- Task.fromFuture(strategy.find(code))
      _ <- Task.fromFuture(strategy.remove(code))
    } yield r

  def issueActivateCode(userId: String, mail: String): Task[Unit] = {
    for {
      code <- Task.pure(generateCode)
      _ <- Task.fromFuture(strategy.store(code, userId))
      _ <- sendActivateCodeEmailTo(mail, code)
    } yield ()
  }

  def activateAccount(
      code: String): M[Either[AuthenticationCustomError, Unit]] =
    (for {
      id <- EitherT[M, AuthenticationCustomError, String] {
        Kleisli.liftF(
          findActivateCode(code).map(_.toRight(InvalidActivateCodeError)))
      }
      _ <- userCredentialsDao
        .activate(id, activated = true)
        .toRight[AuthenticationCustomError](ActivateAccountError)
    } yield ()).value

  def issueInitializationCode(
      mail: String): M[Either[AuthenticationCustomError, Unit]] = {
    (for {
      recode <- userCredentialsDao
        .resolveBySigninId(mail)
        .toRight(AccountNotFoundError)
      code = generateCode
      _ <- EitherT[M, AuthenticationCustomError, Unit] {
        Kleisli.liftF {
          (for {
            _ <- Task.fromFuture(strategy.store(code, recode.id))
            _ <- sendInitializationCodeEmailTo(mail, code)
          } yield ()).map(_ => Right())
        }
      }
    } yield ()).value
  }

  def initAccountPassword(
      code: String): M[Either[AuthenticationCustomError, String]] =
    (for {
      id <- EitherT[M, AuthenticationCustomError, String] {
        Kleisli.liftF(
          findActivateCode(code).map(_.toRight(InvalidInitializationCodeError)))
      }
      newPlainPassword = "newPlainPassword"
      _ <- userCredentialsDao
        .updatePassword(id, newPlainPassword)
        .toRight[AuthenticationCustomError](ActivateAccountError)
    } yield newPlainPassword).value

}
