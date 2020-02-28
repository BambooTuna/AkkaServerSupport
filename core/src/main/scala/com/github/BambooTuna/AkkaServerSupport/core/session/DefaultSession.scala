package com.github.BambooTuna.AkkaServerSupport.core.session

import akka.http.scaladsl.server.{
  AuthorizationFailedRejection,
  Directive0,
  Directive1,
  MissingHeaderRejection,
  ValidationRejection
}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import com.github.BambooTuna.AkkaServerSupport.core.session.model.{
  Session,
  SessionSerializer,
  SessionStorageStrategy,
  StringSessionSerializer
}
import pdi.jwt.{Jwt, JwtClaim}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class DefaultSession[V](override val settings: DefaultSessionSettings)(
    implicit strategy: SessionStorageStrategy[String, String],
    ss: SessionSerializer[V, String],
    executor: ExecutionContext)
    extends Session[String, V] {

  val sts: SessionSerializer[String, String] =
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
    Jwt.decode(value, settings.token, Seq(settings.algorithm))

  override def setSession(token: V): Directive0 = {
    val id = settings.createTokenId
    val tokenValue = jwtEncode(id, ss.serialize(token))
    val f =
      strategy.store(id, ss.serialize(token))
    onComplete(f).flatMap {
      case Success(_) => addAuthHeader(tokenValue)
      case Failure(e) => reject(ValidationRejection(e.getMessage))
    }
  }

  override def requiredSession: Directive1[V] =
    optionalHeaderValueByName(settings.setAuthHeaderName)
      .flatMap {
        case Some(value) =>
          val f =
            for {
              key <- Future.fromTry(jwtDecode(value))
              v <- strategy.find(key.jwtId.get)
              content <- Future.successful(
                v.filter(a => {
                    println(s"$a|${key.content}"); a == key.content
                  })
                  .flatMap(a => ss.deserialize(a).toOption))
            } yield content
          onComplete(f).flatMap {
            case Success(Some(value)) => provide(value)
            case Success(None)        => reject(AuthorizationFailedRejection)
            case Failure(e)           => reject(ValidationRejection(e.getMessage))
          }
        case None => reject(MissingHeaderRejection(settings.setAuthHeaderName))
      }

  override def invalidateSession(): Directive0 =
    optionalHeaderValueByName(settings.setAuthHeaderName)
      .flatMap {
        case Some(value) => invalidateSession(value)
        case None        => reject(MissingHeaderRejection(settings.setAuthHeaderName))
      }

  override def invalidateSession(value: String): Directive0 = {
    val f =
      for {
        key <- Future.fromTry(jwtDecode(value))
        r <- strategy.remove(key.jwtId.get)
      } yield r
    onComplete(f).flatMap {
      case Success(_) => pass
      case Failure(e) => reject(AuthorizationFailedRejection)
    }
  }

}
