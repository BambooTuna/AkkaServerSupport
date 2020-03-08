package com.github.BambooTuna.AkkaServerSupport.authentication.useCase.oauth2

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{FormData, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import cats.data.EitherT
import com.github.BambooTuna.AkkaServerSupport.authentication.error.{
  AccessTokenAcquisitionResponseFailedError,
  AccessTokenAcquisitionUseCaseError,
  CSRFTokenForbiddenError
}
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.serializer.AccessTokenAcquisitionSerializer
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.{
  AccessTokenAcquisitionRequest,
  AccessTokenAcquisitionResponse,
  ClientAuthenticationResponse,
  ClientConfig
}
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import io.circe._
import io.circe.syntax._
import monix.eval.Task

import scala.concurrent.ExecutionContext

class AccessTokenAcquisitionUseCase[I <: AccessTokenAcquisitionRequest,
                                    O <: AccessTokenAcquisitionResponse](
    clientConfig: ClientConfig,
    strategy: StorageStrategy[String, String])(
    implicit system: ActorSystem,
    mat: Materializer,
    executor: ExecutionContext,
    as: AccessTokenAcquisitionSerializer[I]) {

  def execute(res: ClientAuthenticationResponse)(
      implicit i: Encoder[I],
      o: Decoder[O]): EitherT[Task, AccessTokenAcquisitionUseCaseError, O] = {
    implicit val executor: ExecutionContext = system.dispatcher
    val request = as.serialize(clientConfig, res.code)
    for {
      _ <- checkCacheRequestState(res.state)
      r <- runHttpRequest(request)
    } yield r
  }

  private def runHttpRequest(request: I)(
      implicit i: Encoder[I],
      o: Decoder[O]): EitherT[Task, AccessTokenAcquisitionUseCaseError, O] = {
    val httpRequest = accessTokenAcquisitionRequest(request)
    EitherT {
      Task.fromFuture(
        Http()
          .singleRequest(httpRequest)
          .flatMap(r => {
            Unmarshal(r)
              .to[String]
              .map(
                u =>
                  parser
                    .decode[O](u)
                    .left
                    .map(_ => AccessTokenAcquisitionResponseFailedError))
          })
      )
    }
  }

  private def checkCacheRequestState(stats: String)
    : EitherT[Task, AccessTokenAcquisitionUseCaseError, Unit] = {
    EitherT {
      Task.fromFuture(
        for {
          _ <- strategy
            .find(stats)
            .map(_.filter(_ == clientConfig.serviceName)
              .toRight(CSRFTokenForbiddenError))
          r <- strategy.remove(stats).map(_.toRight(CSRFTokenForbiddenError))
        } yield r
      )
    }
  }

  private def accessTokenAcquisitionRequest(request: I)(
      implicit e: Encoder[I]): HttpRequest = {
    val uri = clientConfig.accessTokenIssuanceUri
    val query =
      request.asJson.dropNullValues.asObject
        .fold(Map.empty[String, String])(
          _.toMap.mapValues(_.toString().replace("\"", "")))
    HttpRequest(
      method = HttpMethods.POST,
      uri = uri,
      entity = FormData(query).toEntity
    )
  }

}
