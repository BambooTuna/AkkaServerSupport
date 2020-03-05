package com.github.BambooTuna.AkkaServerSupport.sample.dao

import cats.data.{Kleisli, OptionT}
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.LinkedUserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.sample.model.LinkedUserCredentialsImpl
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import monix.eval.Task

class LinkedUserCredentialsDaoImpl extends LinkedUserCredentialsDao {
  override type IO[O] = Task[O]
  override type DBSession = Resource[IO, HikariTransactor[IO]]
  override type Record = LinkedUserCredentialsImpl
  override type Id = Record#Id

  val dc: DoobieContext.MySQL[SnakeCase] = new DoobieContext.MySQL(SnakeCase)
  import dc._
  import doobie.implicits._

  implicit lazy val daoSchemaMeta =
    schemaMeta[Record](
      "linked_user_credentials",
      _.id -> "id",
      _.service -> "service_name",
    )

  override def insert(
      record: LinkedUserCredentialsImpl): M[LinkedUserCredentialsImpl] =
    Kleisli { implicit ctx: DBSession =>
      val q = quote {
        query[Record]
          .insert(lift(record))
      }
      ctx
        .use(x => run(q).transact(x))
        .map(a => if (a > 0) record else throw new RuntimeException)
    }

  override def resolveById(id: String): OptionT[M, LinkedUserCredentialsImpl] =
    OptionT[M, Record] {
      Kleisli { implicit ctx: DBSession =>
        val q = quote {
          query[Record]
            .filter(_.id == lift(id))
        }
        ctx
          .use(x => run(q).transact(x))
          .map(_.headOption)
      }
    }

  override def delete(id: String): OptionT[M, String] =
    OptionT[M, Id] {
      Kleisli { implicit ctx: DBSession =>
        val q = quote {
          query[Record]
            .filter(_.id == lift(id))
            .delete
        }
        ctx
          .use(x => run(q).transact(x))
          .map(a => if (a > 0) Some(id) else None)
      }
    }

}
