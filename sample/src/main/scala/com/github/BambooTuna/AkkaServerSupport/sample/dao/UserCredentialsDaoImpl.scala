package com.github.BambooTuna.AkkaServerSupport.sample.dao

import cats.data.{Kleisli, OptionT}
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.sample.model.{
  EncryptedPasswordImpl,
  UserCredentialsImpl
}
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import monix.eval.Task

class UserCredentialsDaoImpl extends UserCredentialsDao[UserCredentialsImpl] {
  override type DBSession = Resource[Task, HikariTransactor[Task]]

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

  override def insert(record: UserCredentialsImpl): M[UserCredentialsImpl] =
    Kleisli { implicit ctx =>
      val q = quote {
        query[UserCredentialsImpl]
          .insert(lift(record))
      }
      ctx
        .use(x => run(q).transact(x))
        .map(a => if (a > 0) record else throw new RuntimeException)
    }

  override def resolveBySigninId(
      signinId: String): OptionT[M, UserCredentialsImpl] =
    OptionT[M, UserCredentialsImpl] {
      Kleisli { implicit ctx =>
        val q = quote {
          query[UserCredentialsImpl]
            .filter(_.signinId == lift(signinId))
        }
        ctx
          .use(x => run(q).transact(x))
          .map(_.headOption)
      }
    }

  override def updatePassword(id: String,
                              newPlainPassword: String): OptionT[M, Unit] =
    OptionT[M, Unit] {
      Kleisli { implicit ctx =>
        val newRecode =
          EncryptedPasswordImpl("").changeEncryptedPass(newPlainPassword)
        val q = quote {
          query[UserCredentialsImpl]
            .filter(_.id == lift(id))
            .update(_.signinPass.encryptedPass -> lift(newRecode.encryptedPass))
        }
        ctx
          .use(x => run(q).transact(x))
          .map(a => if (a > 0) Some() else None)
      }
    }

  override def activate(id: String, activated: Boolean): OptionT[M, Unit] =
    OptionT[M, Unit] {
      Kleisli { implicit ctx =>
        val q = quote {
          query[UserCredentialsImpl]
            .filter(_.id == lift(id))
            .update(_.activated -> lift(activated))
        }
        ctx
          .use(x => run(q).transact(x))
          .map(a => if (a > 0) Some() else None)
      }
    }

  override def delete(signinId: String): OptionT[M, Unit] =
    OptionT[M, Unit] {
      Kleisli { implicit ctx =>
        val q = quote {
          query[UserCredentialsImpl]
            .filter(_.signinId == lift(signinId))
            .delete
        }
        ctx
          .use(x => run(q).transact(x))
          .map(a => if (a > 0) Some() else None)
      }
    }

}
