package com.github.BambooTuna.AkkaServerSupport.sample.dao

import cats.data.{Kleisli, OptionT}
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.UserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.sample.model.UserCredentialsImpl
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import monix.eval.Task

class UserCredentialsDaoImpl extends UserCredentialsDao {

  override type IO[O] = Task[O]
  override type DBSession = Resource[IO, HikariTransactor[IO]]

  override type Record = UserCredentialsImpl

  override type Id = Record#SigninId

  val dc: DoobieContext.MySQL[SnakeCase] = new DoobieContext.MySQL(SnakeCase)
  import dc._
  import doobie.implicits._

  implicit lazy val daoSchemaMeta =
    schemaMeta[Record](
      "user_credentials",
      _.id -> "id",
      _.signinId -> "mail",
      _.signinPass.encryptedPass -> "pass"
    )

  override def insert(record: Record): M[Record] =
    Kleisli { implicit ctx: DBSession =>
      val q = quote {
        query[Record]
          .insert(lift(record))
      }
      ctx
        .use(x => run(q).transact(x))
        .map(a => if (a > 0) record else throw new RuntimeException)
    }

  override def resolveById(signinId: String): OptionT[M, Record] =
    OptionT[M, Record] {
      Kleisli { implicit ctx: DBSession =>
        val q = quote {
          query[Record]
            .filter(_.signinId == lift(signinId))
        }
        ctx
          .use(x => run(q).transact(x))
          .map(_.headOption)
      }
    }

  override def update(record: Record): OptionT[M, Record] =
    OptionT[M, Record] {
      Kleisli { implicit ctx: DBSession =>
        val q = quote {
          query[Record]
            .filter(_.signinId == lift(record.signinId))
            .update(a =>
              (a.signinId -> record.signinId,
               a.signinPass -> record.signinPass))
        }
        ctx
          .use(x => run(q).transact(x))
          .map(a => if (a > 0) Some(record) else None)
      }
    }

  override def delete(signinId: String): OptionT[M, Id] =
    OptionT[M, Id] {
      Kleisli { implicit ctx: DBSession =>
        val q = quote {
          query[Record]
            .filter(_.signinId == lift(signinId))
            .delete
        }
        ctx
          .use(x => run(q).transact(x))
          .map(a => if (a > 0) Some(signinId) else None)
      }
    }

}
