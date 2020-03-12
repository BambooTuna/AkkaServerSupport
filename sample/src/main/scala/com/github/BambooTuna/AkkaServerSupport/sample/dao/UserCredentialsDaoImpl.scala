package com.github.BambooTuna.AkkaServerSupport.sample.dao

import cats.data.OptionT
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import monix.eval.Task

class UserCredentialsDaoImpl(dbSession: Resource[Task, HikariTransactor[Task]])
    extends UserCredentialsDao[UserCredentialsImpl] {

  val dc: DoobieContext.MySQL[SnakeCase] = new DoobieContext.MySQL(SnakeCase)
  import dc._
  import doobie.implicits._

  implicit lazy val daoSchemaMeta =
    schemaMeta[UserCredentialsImpl](
      "user_credentials",
      _.id -> "id",
      _.signinId -> "mail",
      _.signinPass.encryptedPass -> "pass",
      _.activated -> "activated"
    )

  override def insert(
      record: UserCredentialsImpl): Task[UserCredentialsImpl] = {
    val q = quote {
      query[UserCredentialsImpl]
        .insert(lift(record))
    }
    dbSession
      .use(x => run(q).transact(x))
      .map(a => if (a > 0) record else throw new RuntimeException)
  }

  override def resolveById(id: String): OptionT[Task, UserCredentialsImpl] =
    OptionT[Task, UserCredentialsImpl] {
      val q = quote {
        query[UserCredentialsImpl]
          .filter(_.id == lift(id))
      }
      dbSession
        .use(x => run(q).transact(x))
        .map(_.headOption)
    }

  override def resolveBySigninId(
      signinId: String): OptionT[Task, UserCredentialsImpl] =
    OptionT[Task, UserCredentialsImpl] {
      val q = quote {
        query[UserCredentialsImpl]
          .filter(_.signinId == lift(signinId))
      }
      dbSession
        .use(x => run(q).transact(x))
        .map(_.headOption)
    }

  override def updatePassword(
      id: String,
      newEncryptedPassword: String): OptionT[Task, Unit] =
    OptionT[Task, Unit] {
      val q = quote {
        query[UserCredentialsImpl]
          .filter(_.id == lift(id))
          .update(_.signinPass.encryptedPass -> lift(newEncryptedPassword))
      }
      dbSession
        .use(x => run(q).transact(x))
        .map(a => if (a > 0) Some() else None)
    }

  override def activate(id: String, activated: Boolean): OptionT[Task, Unit] =
    OptionT[Task, Unit] {
      val q = quote {
        query[UserCredentialsImpl]
          .filter(_.id == lift(id))
          .update(_.activated -> lift(activated))
      }
      dbSession
        .use(x => run(q).transact(x))
        .map(a => if (a > 0) Some() else None)
    }

  override def delete(signinId: String): OptionT[Task, Unit] =
    OptionT[Task, Unit] {
      val q = quote {
        query[UserCredentialsImpl]
          .filter(_.signinId == lift(signinId))
          .delete
      }
      dbSession
        .use(x => run(q).transact(x))
        .map(a => if (a > 0) Some() else None)
    }

}
