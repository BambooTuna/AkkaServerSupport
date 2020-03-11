package com.github.BambooTuna.AkkaServerSupport.sample.dao

import cats.data.{Kleisli, OptionT}
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.dao.LinkedUserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import monix.eval.Task

class LinkedUserCredentialsDaoImpl extends LinkedUserCredentialsDao {
  override type DBSession = Resource[Task, HikariTransactor[Task]]

  val dc: DoobieContext.MySQL[SnakeCase] = new DoobieContext.MySQL(SnakeCase)
  import dc._
  import doobie.implicits._

  implicit lazy val daoSchemaMeta =
    schemaMeta[LinkedUserCredentials](
      "linked_user_credentials",
      _.id -> "id",
      _.serviceId -> "service_id",
      _.serviceName -> "service_name",
    )

  override def insert(record: LinkedUserCredentials): M[LinkedUserCredentials] =
    Kleisli { implicit ctx =>
      val q = quote {
        query[LinkedUserCredentials]
          .insert(lift(record))
      }
      ctx
        .use(x => run(q).transact(x))
        .map(a => if (a > 0) record else throw new RuntimeException)
    }

  override def resolveById(id: String): OptionT[M, LinkedUserCredentials] =
    OptionT[M, LinkedUserCredentials] {
      Kleisli { implicit ctx =>
        val q = quote {
          query[LinkedUserCredentials]
            .filter(_.id == lift(id))
        }
        ctx
          .use(x => run(q).transact(x))
          .map(_.headOption)
      }
    }

  override def resolveByServiceId(
      serviceId: String): OptionT[M, LinkedUserCredentials] =
    OptionT[M, LinkedUserCredentials] {
      Kleisli { implicit ctx =>
        val q = quote {
          query[LinkedUserCredentials]
            .filter(_.serviceId == lift(serviceId))
        }
        ctx
          .use(x => run(q).transact(x))
          .map(_.headOption)
      }
    }

  override def delete(serviceId: String): OptionT[M, Unit] =
    OptionT[M, Unit] {
      Kleisli { implicit ctx =>
        val q = quote {
          query[LinkedUserCredentials]
            .filter(_.serviceId == lift(serviceId))
            .delete
        }
        ctx
          .use(x => run(q).transact(x))
          .map(a => if (a > 0) Some() else None)
      }
    }

}
