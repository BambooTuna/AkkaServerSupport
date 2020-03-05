package com.github.BambooTuna.AkkaServerSupport.sample.oauth

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{
  complete,
  onComplete,
  parameterMap,
  redirect
}
import akka.stream.Materializer
import cats.data.{EitherT, Kleisli}
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.router.RouteSupport
import com.github.BambooTuna.AkkaServerSupport.authentication.router.RouteSupport.{
  QueryParameterParseError,
  SessionToken
}
import com.github.BambooTuna.AkkaServerSupport.authentication.router.error.CustomError
import com.github.BambooTuna.AkkaServerSupport.authentication.session.DefaultSession
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.LinkedAuthenticationUseCase.LinkedAuthenticationUseCaseError
import com.github.BambooTuna.AkkaServerSupport.cooperation.model.OAuth2Settings
import com.github.BambooTuna.AkkaServerSupport.sample.command.{
  LinkedSignInRequestCommandImpl,
  LinkedSignUpRequestCommandImpl
}
import com.github.BambooTuna.AkkaServerSupport.sample.useCase.LinkedAuthenticationUseCaseImpl
import doobie.hikari.HikariTransactor

import scala.concurrent.ExecutionContext
import monix.execution.Scheduler.Implicits.global
import io.circe.syntax._
import io.circe.generic.auto._
import monix.eval.Task
import pdi.jwt.algorithms.JwtHmacAlgorithm
import pdi.jwt.JwtAlgorithm

import scala.util.{Failure, Success}

abstract class LineOAuth2RouteImpl(
    val session: DefaultSession[SessionToken],
    val settings: OAuth2Settings,
)(implicit system: ActorSystem,
  mat: Materializer,
  val executor: ExecutionContext)
    extends RouteSupport {

  val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256

  val useCase: LineOAuth2UseCaseImpl =
    new LineOAuth2UseCaseImpl(settings)

  val authenticationUseCase =
    new LinkedAuthenticationUseCaseImpl

  def authenticationCodeIssuanceRoute: QueryP[Unit] = _ {
    val f =
      useCase.runAuthenticationCodeIssuance.runToFuture
    onComplete(f) {
      case Success(value) =>
        redirect(value, StatusCodes.PermanentRedirect)
      case Failure(exception) =>
        errorHandling(exception)
    }
  }

  def getAccessTokenFromAuthenticationCodeRoute(
      dbSession: Resource[Task, HikariTransactor[Task]]): QueryP[Unit] = _ {
    parameterMap { m =>
      m.asJson.as[useCase.ClientAuthenticationResponseSuccessType] match {
        case Right(value) =>
          val f =
            (for {
              res <- useCase
                .runAccessTokenAcquisitionRequest(value)
              id <- EitherT { Task.pure(useCase.decodeIdToken(res)) }
              r <- EitherT[Task, CustomError, authenticationUseCase.Record] {
                authenticationUseCase
                  .signUp(
                    LinkedSignUpRequestCommandImpl(
                      id,
                      settings.clientConfig.serviceName))
                  .flatMap {
                    case Right(value) =>
                      Kleisli.liftF[
                        authenticationUseCase.IO,
                        authenticationUseCase.linkedUserCredentialsDao.DBSession,
                        Either[LinkedAuthenticationUseCaseError,
                               authenticationUseCase.Record]] {
                        Task.pure(Right(value))
                      }
                    case Left(_) =>
                      authenticationUseCase.signIn(
                        LinkedSignInRequestCommandImpl(
                          id,
                          settings.clientConfig.serviceName))
                  }
                  .run(dbSession)
              }
            } yield r).value.runToFuture

          onComplete(f) {
            case Success(Right(value)) =>
              session
                .setSession(
                  SessionToken(value.id,
                               Some(settings.clientConfig.serviceName))) {
                  complete(StatusCodes.OK)
                }
            case Success(Left(value)) =>
              println(value.toString)
              customErrorHandler(value)
            case Failure(exception) =>
              println(exception.getMessage)
              errorHandling(exception)
          }
        case Left(_) => customErrorHandler(QueryParameterParseError)
      }
    }
  }

}
