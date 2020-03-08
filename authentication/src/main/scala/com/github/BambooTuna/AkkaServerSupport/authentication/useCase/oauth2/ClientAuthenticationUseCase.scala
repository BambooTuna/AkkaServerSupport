package com.github.BambooTuna.AkkaServerSupport.authentication.useCase.oauth2

import akka.http.scaladsl.model.Uri
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.serializer.ClientAuthenticationSerializer
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.{ClientAuthenticationRequest, ClientConfig}
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.oauth2.ClientAuthenticationUseCase.ClientAuthenticationResult
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import io.circe.syntax._
import io.circe.generic.auto._
import monix.eval.Task

class ClientAuthenticationUseCase[I <: ClientAuthenticationRequest](clientConfig: ClientConfig, strategy: StorageStrategy[String, String])(implicit cs: ClientAuthenticationSerializer[I]) {

  def execute: Task[ClientAuthenticationResult] = {
    val request = cs.serialize(clientConfig)
    val uri = issueLink(request)
    cacheRequestState(request).map(_ => ClientAuthenticationResult(uri.toString()))
  }

  private def cacheRequestState(request: I): Task[Option[Unit]] = {
    Task.fromFuture(strategy.store(request.state, clientConfig.serviceName))
  }

  private def issueLink(request: I): Uri = {
    val query =
      request.asJson.dropNullValues.asObject
        .fold(Map.empty[String, String])(
          _.toMap.mapValues(_.toString().replace("\"", "")))
    clientConfig.authenticationCodeIssuanceUri
      .withQuery(Uri.Query(query))
  }

}

object ClientAuthenticationUseCase {
  case class ClientAuthenticationResult(redirectUri: String)
}
