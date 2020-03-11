package com.github.BambooTuna.AkkaServerSupport.sample.controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import cats.effect.Resource
import com.github.BambooTuna.AkkaServerSupport.authentication.controller.OAuth2Controller
import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.ClientConfig
import com.github.BambooTuna.AkkaServerSupport.authentication.session.SessionToken
import com.github.BambooTuna.AkkaServerSupport.cooperation.line.{
  LineAccessTokenAcquisitionRequest,
  LineAccessTokenAcquisitionResponse,
  LineClientAuthenticationRequest
}
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  Session,
  StorageStrategy
}
import com.github.BambooTuna.AkkaServerSupport.sample.useCase.LinkedAuthenticationUseCaseImpl
import doobie.hikari.HikariTransactor
import io.circe.generic.auto._
import monix.eval.Task

import scala.concurrent.ExecutionContext

class LineOAuth2ControllerImpl(
    clientConfig: ClientConfig,
    strategy: StorageStrategy[String, String],
    val dbSession: Resource[Task, HikariTransactor[Task]])(
    implicit system: ActorSystem,
    mat: Materializer,
    executor: ExecutionContext,
    session: Session[String, SessionToken])
    extends OAuth2Controller[LineClientAuthenticationRequest,
                             LineAccessTokenAcquisitionRequest,
                             LineAccessTokenAcquisitionResponse](clientConfig,
                                                                 strategy) {
  override val linkedAuthenticationUseCase: LinkedAuthenticationUseCaseImpl =
    new LinkedAuthenticationUseCaseImpl
}
