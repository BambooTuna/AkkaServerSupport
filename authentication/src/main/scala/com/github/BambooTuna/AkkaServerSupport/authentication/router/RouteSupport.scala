package com.github.BambooTuna.AkkaServerSupport.authentication.router

import akka.http.scaladsl.server.StandardRoute
import com.github.BambooTuna.AkkaServerSupport.authentication.router.RouteSupport.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  DefaultSession,
  DefaultSessionSettings
}
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase.AuthenticationUseCaseError
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  SessionSerializer,
  SessionStorageStrategy,
  StringSessionSerializer
}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

trait RouteSupport extends FailFastCirceSupport {

  implicit val executor: ExecutionContext
  implicit val settings: DefaultSessionSettings
  implicit val strategy: SessionStorageStrategy[String, String]
  def errorHandling(throwable: Throwable): StandardRoute

  implicit def serializer: SessionSerializer[SessionToken, String] =
    new StringSessionSerializer(
      _.asJson.noSpaces,
      (in: String) => parser.decode[SessionToken](in).toTry)
  protected lazy val session: DefaultSession[SessionToken] =
    new DefaultSession[SessionToken](settings) {
      override def fromThrowable(throwable: Throwable): StandardRoute =
        errorHandling(throwable)
    }

  def customErrorHandler(error: AuthenticationUseCaseError): StandardRoute

}

object RouteSupport {
  case class SessionToken(userId: String)
}
