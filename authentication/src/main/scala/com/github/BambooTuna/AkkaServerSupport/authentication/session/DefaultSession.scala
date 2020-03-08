package com.github.BambooTuna.AkkaServerSupport.authentication.session

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive0, Directive1, MissingHeaderRejection}
import cats._
import data._
import implicits._
import cats.data.OptionT
import com.github.BambooTuna.AkkaServerSupport.core.session.{Session, SessionSerializer, StorageStrategy, StringSessionSerializer}
import pdi.jwt.{Jwt, JwtClaim}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

abstract class DefaultSession[V](val settings: JWTSessionSettings,
                                 val strategy: StorageStrategy[String, String])(
    implicit ss: SessionSerializer[V, String],
    executor: ExecutionContext)
    extends Session[String, V] {

  protected val sts: SessionSerializer[String, String] =
    new StringSessionSerializer(identity, (in: String) => Try { in })

  def jwtEncode(id: String, value: String): String =
    Jwt.encode(
      JwtClaim(value)
        .issuedNow(settings.clock)
        .expiresIn(settings.expirationDate.toSeconds)(settings.clock)
        .withId(id),
      settings.token,
      settings.algorithm
    )

  def jwtDecode(value: String): Try[JwtClaim] =
    Jwt
      .decode(value, settings.token, Seq(settings.algorithm))

  override def setSession(token: V): Directive0 = {
    val id: String = settings.createTokenId
    val tokenValue = jwtEncode(id, ss.serialize(token))
    val f =
      strategy
        .store(id, ss.serialize(token))
    onSuccess(f).flatMap(_ => addAuthHeader(tokenValue))
  }

  override def requiredSession: Directive1[V] =
    optionalHeaderValueByName(settings.authHeaderName)
      .flatMap {
        case Some(value) =>
          val f =
            (for {
              key <- OptionT[Future, JwtClaim](
                Future.successful(jwtDecode(value).toOption))
              jwtId <- OptionT[Future, String](Future.successful(key.jwtId))
              v <- OptionT[Future, String](strategy.find(jwtId)).filter(
                _ == key.content)
              content <- OptionT[Future, V](
                Future.successful(ss.deserialize(v).toOption))
            } yield content).value
          onSuccess(f).flatMap {
            case Some(value) => provide(value)
            case None  => reject(AuthorizationFailedRejection)
          }
        case None => reject(MissingHeaderRejection(settings.authHeaderName))
      }

  override def invalidateSession(): Directive0 =
    optionalHeaderValueByName(settings.authHeaderName)
      .flatMap {
        case Some(value) => invalidateSession(value)
        case None        => reject(MissingHeaderRejection(settings.authHeaderName))
      }

  override def invalidateSession(value: String): Directive0 = {
    val f =
      (for {
        key <- OptionT[Future, JwtClaim](
          Future.successful(jwtDecode(value).toOption))
        jwtId <- OptionT[Future, String](Future.successful(key.jwtId))
        _ <- OptionT[Future, String](strategy.find(jwtId)).filter(
          _ == key.content)
        r <- OptionT[Future, Unit](strategy.remove(jwtId).map(_ => Some()))
      } yield r).value
    onSuccess(f).flatMap {
      case Some(_) => pass
      case None  => reject(AuthorizationFailedRejection)
    }
  }

}
