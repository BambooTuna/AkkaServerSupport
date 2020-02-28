package com.github.BambooTuna.AkkaServerSupport.core.session
import akka.http.scaladsl.server.{
  AuthorizationFailedRejection,
  Directive0,
  Directive1,
  MissingHeaderRejection
}
import akka.http.scaladsl.server.Directives.{
  optionalHeaderValueByName,
  provide,
  pass
}
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import com.github.BambooTuna.AkkaServerSupport.core.session.model.SessionSerializer

import scala.util.{Failure, Success, Try}

case class DefaultSessionSettings[V](token: String, authHeaderName: String)(
    implicit strategy: SessionStorageStrategy[String, V],
    ss: SessionSerializer[V, String])
    extends SessionSettings[String, V] {

  override def decodeHeaderValue(value: String): Try[V] = Try {
    strategy.find(findKey(value)).get
  }

  override def findKey(value: String): String = value

  override def setSession(token: V): Directive0 = {
    val key = ss.serialize(token)
    strategy.store(key, token)
    pass
  }

  override def requiredSession: Directive1[V] =
    optionalHeaderValueByName(authHeaderName)
      .flatMap {
        case Some(value) =>
          this.decodeHeaderValue(value) match {
            case Success(value) => provide(value)
            case Failure(_)     => reject(AuthorizationFailedRejection)
          }
        case None => reject(MissingHeaderRejection(authHeaderName))
      }

  override def invalidateSession(): Directive0 =
    requiredSession.flatMap(invalidateSession)

  override def invalidateSession(token: V): Directive0 = {
    val key = ss.serialize(token)
    strategy.remove(key)
    pass
  }
}
