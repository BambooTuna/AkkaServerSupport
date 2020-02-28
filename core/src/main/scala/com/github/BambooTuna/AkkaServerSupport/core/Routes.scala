package com.github.BambooTuna.AkkaServerSupport.core

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server._
import com.github.BambooTuna.AkkaServerSupport.core.router._
import com.github.BambooTuna.AkkaServerSupport.core.session.model.{
  SessionSerializer,
  StringSessionSerializer
}
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  DefaultSessionSettings,
  DefaultSessionStorageStrategy,
  SessionStorageStrategy
}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.Try

object Routes {
  type QueryP[T] = Directive[T] => Route

  case class Token(value: String)

  implicit def serializer: SessionSerializer[Token, String] =
    StringSessionSerializer(_.value, (in: String) => Try { Token(in) })
  implicit val strategy: SessionStorageStrategy[String, Token] =
    DefaultSessionStorageStrategy()(serializer)
  val settings: DefaultSessionSettings[Token] =
    DefaultSessionSettings[Token]("pass", "Set-Auth")

  def set: QueryP[Tuple1[String]] = _ { path =>
    settings.setSession(Token(path)) {
      complete(StatusCodes.OK, s"path: $path")
    }
  }

  def login: QueryP[Unit] = _ {
    settings.requiredSession { s =>
      complete(StatusCodes.OK, s"header: $s")
    }
  }

  def logOut: QueryP[Unit] = _ {
    settings.invalidateSession() {
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
