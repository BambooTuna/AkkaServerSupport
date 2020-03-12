package com.github.BambooTuna.AkkaServerSupport.authentication.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive, Route}
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import cats.data.EitherT
import com.github.BambooTuna.AkkaServerSupport.authentication.error.ParseParameterFailedError
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.serializer.{
  AccessTokenAcquisitionResponseParser,
  AccessTokenAcquisitionSerializer,
  ClientAuthenticationSerializer
}
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.{
  AccessTokenAcquisitionRequest,
  AccessTokenAcquisitionResponse,
  ClientAuthenticationRequest,
  ClientAuthenticationResponse,
  ClientConfig
}
import com.github.BambooTuna.AkkaServerSupport.authentication.session.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.LinkedAuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.oauth2.{
  AccessTokenAcquisitionUseCase,
  ClientAuthenticationUseCase
}
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  Session,
  StorageStrategy
}
import monix.execution.Scheduler
import io.circe.syntax._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.{Decoder, Encoder}
import monix.eval.Task

import scala.concurrent.{ExecutionContext, Future}

abstract class OAuth2Controller[CI <: ClientAuthenticationRequest,
                                AI <: AccessTokenAcquisitionRequest,
                                AO <: AccessTokenAcquisitionResponse](
    implicit system: ActorSystem,
    mat: Materializer,
    executor: ExecutionContext,
    session: Session[String, SessionToken],
    cs: ClientAuthenticationSerializer[CI],
    as: AccessTokenAcquisitionSerializer[AI],
    ap: AccessTokenAcquisitionResponseParser[AO],
    eCI: Encoder[CI],
    eAI: Encoder[AI],
    dAO: Decoder[AO]) {

  type QueryP[Q] = Directive[Q] => Route

  val clientConfig: ClientConfig
  val cacheStorage: StorageStrategy[String, String]

  val linkedAuthenticationUseCase: LinkedAuthenticationUseCase

  private val clientAuthenticationUseCase: ClientAuthenticationUseCase[CI] =
    new ClientAuthenticationUseCase(clientConfig, cacheStorage)
  private val accessTokenAcquisitionUseCase
    : AccessTokenAcquisitionUseCase[AI, AO] =
    new AccessTokenAcquisitionUseCase(clientConfig, cacheStorage)

  def fetchCooperationLink(implicit s: Scheduler): QueryP[Unit] = _ {
    val f: Future[ClientAuthenticationUseCase.ClientAuthenticationResult] =
      clientAuthenticationUseCase.execute.runToFuture
    onSuccess(f) { uri =>
      complete(StatusCodes.OK -> uri)
    }
  }

  def authenticationFromCode(implicit s: Scheduler): QueryP[Unit] = _ {
    parameterMap { m =>
      m.asJson.as[ClientAuthenticationResponse] match {
        case Right(value) =>
          val f =
            (for {
              ao <- accessTokenAcquisitionUseCase.execute(value)
              command <- EitherT {
                Task.pure(ap.parseToSignInCommand(ao, clientConfig))
              }
              result <- for {
                registerCommand <- EitherT {
                  Task.pure(ap.parseToRegisterCommand(ao, clientConfig))
                }
                r <- linkedAuthenticationUseCase
                  .signIn(command)
                  .leftFlatMap(_ =>
                    linkedAuthenticationUseCase.register(registerCommand))
              } yield r
            } yield result).value.runToFuture
          onSuccess(f) {
            case Right(value) =>
              session.setSession(
                SessionToken(value.id, Some(value.serviceName))) {
                complete(StatusCodes.OK)
              }
            case Left(value) => reject(value)
          }
        case Left(_) => reject(ParseParameterFailedError)
      }
    }
  }

}
