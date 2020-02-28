package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import cats.data.{Kleisli, OptionT}
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentialsImpl
import doobie.hikari.HikariTransactor
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import monix.eval.Task

class UserCredentialsDaoImpl extends UserCredentialsDao {

  override type IO[O] = Task[O]
  override type DBSession = Resource[IO, HikariTransactor[IO]]

  override type M[O] = Kleisli[IO, DBSession, O]
  override type Id = String
  override type SignInId = String
  override type Record = UserCredentialsImpl

  val dc: DoobieContext.MySQL[SnakeCase] = new DoobieContext.MySQL(SnakeCase)
  import dc._
  import doobie.implicits._

  implicit lazy val daoSchemaMeta =
    schemaMeta[Record](
      "user_credentials",
      _.id -> "id",
      _.signInId -> "mail",
      _.signInPass.encryptedPass -> "pass"
    )

  override def insert(record: Record): M[Record] =
    Kleisli { implicit ctx: DBSession =>
      val q = quote {
        query[Record]
          .insert(lift(record))
      }
      ctx.use(x => run(q).transact(x)).map(_ => record)
    }

  //TOOD
  override def resolveById(id: String): OptionT[M, Record] = ???
  override def resolveBySignInId(id: SignInId): OptionT[M, Record] = ???
  override def update(record: Record): M[Record] = ???
  override def delete(id: String): M[String] = ???

}
