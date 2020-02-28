package com.github.BambooTuna.AkkaServerSupport.core

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server._
import com.github.BambooTuna.AkkaServerSupport.core.router._
import com.github.BambooTuna.AkkaServerSupport.core.session.model.{
  SessionSerializer,
  SessionStorageStrategy,
  StringSessionSerializer
}
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  DefaultSession,
  DefaultSessionSettings,
  DefaultSessionStorageStrategy
}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

class Routes(implicit executor: ExecutionContext) {
  type QueryP[T] = Directive[T] => Route

  case class Token(value: String)

  implicit def serializer: SessionSerializer[Token, String] =
    new StringSessionSerializer(_.asJson.noSpaces,
                                (in: String) => parser.decode[Token](in).toTry)
  implicit val strategy: SessionStorageStrategy[String, String] =
    new DefaultSessionStorageStrategy()

  val settings: DefaultSessionSettings = new DefaultSessionSettings(
    "internalToken")
  val session: DefaultSession[Token] = new DefaultSession[Token](settings)

  def set: QueryP[Tuple1[String]] = _ { path =>
    session.setSession(Token(path)) {
      complete(StatusCodes.OK, s"path: $path")
    }
  }

  def login: QueryP[Unit] = _ {
    session.requiredSession { s =>
      complete(StatusCodes.OK, s"header: $s")
    }
  }

  def logOut: QueryP[Unit] = _ {
    session.invalidateSession() {
      complete(StatusCodes.OK)
    }
  }

  def createRoute: Router = {
    Router(
      route(GET, "set" / Segment, set),
      route(POST, "login", login),
      route(DELETE, "logout", logOut)
    )
  }
}
