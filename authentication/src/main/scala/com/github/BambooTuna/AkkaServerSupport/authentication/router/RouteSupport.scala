package com.github.BambooTuna.AkkaServerSupport.authentication.router

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.{Directive, Route, StandardRoute}
import akka.http.scaladsl.server.Directives._
import com.github.BambooTuna.AkkaServerSupport.authentication.router.RouteSupport.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.router.error.CustomError
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  DefaultSession,
  JWTSessionSettings
}
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase.AuthenticationUseCaseError
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  SessionSerializer,
  StorageStrategy,
  StringSessionSerializer
}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

trait RouteSupport extends FailFastCirceSupport {

  type QueryP[Q] = Directive[Q] => Route

  protected val session: DefaultSession[SessionToken]

  def errorHandling(throwable: Throwable): StandardRoute
  def customErrorHandler(error: CustomError): StandardRoute =
    complete(error.statusCode, error.message.toString)

}

object RouteSupport {
  implicit def serializer: SessionSerializer[SessionToken, String] =
    new StringSessionSerializer(
      _.asJson.noSpaces,
      (in: String) => parser.decode[SessionToken](in).toTry)

  case class SessionToken(userId: String, cooperation: Option[String] = None)

  sealed trait InRouterError extends CustomError
  case object QueryParameterParseError extends InRouterError {
    override val statusCode: StatusCode = StatusCodes.BadRequest
    override val message: Option[String] = Some("QueryParameterParseError")
  }
}
