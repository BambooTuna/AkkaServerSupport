package com.github.BambooTuna.AkkaServerSupport.sample.controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.BambooTuna.AkkaServerSupport.authentication.controller.OAuth2Controller
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.ClientConfig
import com.github.BambooTuna.AkkaServerSupport.authentication.session.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.LinkedAuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.cooperation.line.{
  LineAccessTokenAcquisitionRequest,
  LineAccessTokenAcquisitionResponse,
  LineClientAuthenticationRequest
}
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  Session,
  StorageStrategy
}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

class LineOAuth2ControllerImpl(
    val linkedAuthenticationUseCase: LinkedAuthenticationUseCase,
    val clientConfig: ClientConfig,
    val cacheStorage: StorageStrategy[String, String])(
    implicit system: ActorSystem,
    mat: Materializer,
    executor: ExecutionContext,
    session: Session[String, SessionToken])
    extends OAuth2Controller[LineClientAuthenticationRequest,
                             LineAccessTokenAcquisitionRequest,
                             LineAccessTokenAcquisitionResponse]
