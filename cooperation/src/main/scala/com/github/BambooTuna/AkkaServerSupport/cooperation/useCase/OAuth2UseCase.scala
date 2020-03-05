package com.github.BambooTuna.AkkaServerSupport.cooperation.useCase

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{
  FormData,
  HttpMethods,
  HttpRequest,
  StatusCode,
  StatusCodes,
  Uri
}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import cats.data.EitherT
import com.github.BambooTuna.AkkaServerSupport.authentication.router.error.CustomError
import com.github.BambooTuna.AkkaServerSupport.cooperation.model.{
  AccessTokenAcquisitionRequest,
  AccessTokenAcquisitionResponseFailed,
  AccessTokenAcquisitionResponseSuccess,
  ClientAuthenticationRequest,
  ClientAuthenticationResponseFailed,
  ClientAuthenticationResponseSuccess,
  ClientConfig,
  OAuth2Settings
}
import com.github.BambooTuna.AkkaServerSupport.cooperation.useCase.OAuth2UseCase.{
  AccessTokenAcquisitionResponseFailedError,
  CSRFTokenForbiddenError,
  HttpRequestError,
  OAuth2UseCaseError
}
import monix.eval.Task
import io.circe._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

abstract class OAuth2UseCase(settings: OAuth2Settings) {

  type ClientAuthenticationRequestType <: ClientAuthenticationRequest
  type ClientAuthenticationResponseSuccessType <: ClientAuthenticationResponseSuccess
  type ClientAuthenticationResponseFailedType <: ClientAuthenticationResponseFailed

  type AccessTokenAcquisitionRequestType <: AccessTokenAcquisitionRequest
  type AccessTokenAcquisitionResponseSuccessType <: AccessTokenAcquisitionResponseSuccess
  type AccessTokenAcquisitionResponseFailedType <: AccessTokenAcquisitionResponseFailed

  def generateClientAuthenticationRequest(
      clientConfig: ClientConfig): ClientAuthenticationRequestType
  def generateAccessTokenAcquisitionRequest(code: String)(
      clientConfig: ClientConfig): AccessTokenAcquisitionRequestType

  protected def authenticationCodeIssuanceUri(
      request: ClientAuthenticationRequestType)(
      implicit encoder: Encoder[ClientAuthenticationRequestType]): Uri = {
    val query =
      request.asJson.dropNullValues.asObject
        .fold(Map.empty[String, String])(
          _.toMap.mapValues(_.toString().replace("\"", "")))
    settings.clientConfig.authenticationCodeIssuanceUri
      .withQuery(Uri.Query(query))
  }

  protected def accessTokenAcquisitionRequest(
      request: AccessTokenAcquisitionRequestType)(
      implicit e: Encoder[AccessTokenAcquisitionRequestType]): HttpRequest = {
    val uri = settings.clientConfig.accessTokenIssuanceUri
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

  def runAuthenticationCodeIssuance(
      implicit encoder: Encoder[ClientAuthenticationRequestType]): Task[Uri] = {
    val request =
      generateClientAuthenticationRequest(settings.clientConfig)
    val uri =
      authenticationCodeIssuanceUri(request)
    Task
      .fromFuture(
        settings.strategy.store(request.state,
                                settings.clientConfig.serviceName))
      .map(_ => uri)
  }

  def runAccessTokenAcquisitionRequest(
      r: ClientAuthenticationResponseSuccessType)(
      implicit system: ActorSystem,
      mat: Materializer,
      e: Encoder[AccessTokenAcquisitionRequestType],
      s: Decoder[AccessTokenAcquisitionResponseSuccessType],
      f: Decoder[AccessTokenAcquisitionResponseFailedType])
    : EitherT[Task,
              OAuth2UseCaseError,
              AccessTokenAcquisitionResponseSuccessType] = {
    implicit val executor: ExecutionContext = system.dispatcher
    val request =
      generateAccessTokenAcquisitionRequest(r.code)(settings.clientConfig)
    val httpRequest = accessTokenAcquisitionRequest(request)
    val key = r.state
    EitherT {
      Task.fromFuture {
        for {
          _ <- settings.strategy
            .find(key)
            .map(_.filter(_ == settings.clientConfig.serviceName)
              .toRight(CSRFTokenForbiddenError))
          _ <- settings.strategy.remove(key)
          r <- Http()
            .singleRequest(httpRequest)
            .flatMap(r => {
              Unmarshal(r)
                .to[String]
                .map(
                  u =>
                    parser
                      .decode[AccessTokenAcquisitionResponseSuccessType](u)
                      .left
                      .flatMap(_ =>
                        parser.decode[AccessTokenAcquisitionResponseFailedType](
                          u) match {
                          case Right(value) =>
                            Left(
                              AccessTokenAcquisitionResponseFailedError(value))
                          case Left(value) => Left(HttpRequestError)
                      }))
            })
        } yield r
      }
    }
  }

}

object OAuth2UseCase {
  sealed trait OAuth2UseCaseError extends CustomError
  case class AccessTokenAcquisitionResponseFailedError(
      response: OAuth2UseCase#AccessTokenAcquisitionResponseFailedType)
      extends OAuth2UseCaseError {
    override val statusCode: StatusCode = StatusCodes.BadRequest
    override val message: Option[String] = response.error_description
  }
  case object CSRFTokenForbiddenError extends OAuth2UseCaseError {
    override val statusCode: StatusCode = StatusCodes.Forbidden
    override val message: Option[String] = Some("CSRFTokenForbiddenError")
  }
  case object HttpRequestError extends OAuth2UseCaseError {
    override val statusCode: StatusCode = StatusCodes.InternalServerError
    override val message: Option[String] = None
  }
}
