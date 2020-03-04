package com.github.BambooTuna.AkkaServerSupport.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, POST}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Route, StandardRoute}
import akka.stream.Materializer
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.session.{
  InMemoryStorageStrategy,
  JWTSessionSettings
}
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase.AuthenticationUseCaseError
import com.github.BambooTuna.AkkaServerSupport.cooperation.line.{
  LineAccessTokenAcquisitionResponseFailed,
  LineAccessTokenAcquisitionResponseSuccess,
  LineClientAuthenticationRequest,
  LineClientAuthenticationResponseSuccess
}
import com.github.BambooTuna.AkkaServerSupport.cooperation.model.{
  ClientAuthenticationRequest,
  ClientConfig
}
import com.github.BambooTuna.AkkaServerSupport.cooperation.useCase.OAuth2UseCase
import com.github.BambooTuna.AkkaServerSupport.core.router.{Router, route}
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import com.github.BambooTuna.AkkaServerSupport.sample.router.AuthenticationRouteImpl
import com.github.BambooTuna.AkkaServerSupport.sample.session.RedisStorageStrategy
import doobie.hikari.HikariTransactor
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.{ExecutionContext, Future}
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

import scala.util.{Failure, Success}

class Routes(sessionSettings: JWTSessionSettings,
             sessionStorage: StorageStrategy[String, String],
             dbSession: Resource[Task, HikariTransactor[Task]])(
    implicit system: ActorSystem,
    mat: Materializer,
    _executor: ExecutionContext) {
  val authenticationRoute =
    new AuthenticationRouteImpl(sessionSettings, sessionStorage) {
      override def convertIO[O](flow: IO[O]): Future[O] =
        flow.runToFuture
      override def errorHandling(throwable: Throwable): StandardRoute = {
        throwable match {
          case e: RuntimeException =>
            complete(StatusCodes.InternalServerError, e.getMessage)
          case e: Exception =>
            complete(StatusCodes.BadRequest, e.getMessage)
        }
      }
      override def customErrorHandler(
          error: AuthenticationUseCaseError): StandardRoute = error match {
        case AuthenticationUseCase.SignUpInsertError =>
          complete(StatusCodes.Conflict, "メールアドレスが使用されています")
        case AuthenticationUseCase.CantFoundUserError =>
          complete(StatusCodes.NotFound, "ユーザーが存在しません")
        case AuthenticationUseCase.SignInIdOrPassWrongError =>
          complete(StatusCodes.Forbidden, "認証失敗")
      }
    }

  def createRoute: Router = {
    val useCase = new OAuth2UseCase {
      override lazy val clientConfig: ClientConfig =
        ClientConfig(
          clientId = "1653910633",
          redirectUri = "http://localhost:8080/oauth2",
          authenticationCodeIssuanceUri = Uri.from(scheme = "https",
                                                   host = "access.line.me",
                                                   port = 443,
                                                   path =
                                                     "/dialog/oauth/weblogin"),
          accessTokenIssuanceUri = Uri.from(scheme = "https",
                                            host = "access.line.me",
                                            port = 443,
                                            path = "/v1/oauth/accessToken",
                                            queryString = None)
        )

      override lazy val clientAuthenticationRequest
        : ClientAuthenticationRequest =
        LineClientAuthenticationRequest.fromConfig(clientConfig)
    }

    Router(
      route(POST, "signup", authenticationRoute.signUpRoute(dbSession)),
      route(POST, "signin", authenticationRoute.signInRoute(dbSession)),
      route(POST,
            "init",
            authenticationRoute.passwordInitializationRoute(dbSession)),
      route(GET, "health", authenticationRoute.healthCheck),
      route(DELETE, "logout", authenticationRoute.logout),
      route(
        GET,
        "signup" / "line",
        (_ {
          redirect(useCase.creatingALinkForObtainingAnAuthenticationCode,
                   StatusCodes.PermanentRedirect)
        }): Directive[Unit] => Route
      ),
      route(
        GET,
        "oauth2",
        (_ {
          parameterMap {
            m =>
              m.asJson.as[LineClientAuthenticationResponseSuccess] match {
                case Right(value) =>
                  val f =
                    useCase
                      .getAccessTokenFromAuthenticationCode[
                        LineAccessTokenAcquisitionResponseSuccess,
                        LineAccessTokenAcquisitionResponseFailed](value)
                      .runToFuture
                  onComplete(f) {
                    case Success(Right(value)) =>
                      complete(StatusCodes.OK)
                    case Success(Left(value)) =>
                      complete(StatusCodes.OK)
                    case Failure(exception) =>
                      complete(StatusCodes.OK, exception.getMessage)
                  }
//                  complete(StatusCodes.OK)
                case Left(value) =>
                  complete(StatusCodes.Forbidden, value.message)
              }
          }
        }): Directive[Unit] => Route
      )
    )
  }

}
