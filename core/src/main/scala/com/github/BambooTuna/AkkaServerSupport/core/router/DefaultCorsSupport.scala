package com.github.BambooTuna.AkkaServerSupport.core.router

import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, OPTIONS, POST, PUT}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.{
  HttpOrigin,
  `Access-Control-Allow-Credentials`,
  `Access-Control-Allow-Headers`,
  `Access-Control-Allow-Methods`,
  `Access-Control-Allow-Origin`,
  `Access-Control-Expose-Headers`
}
import akka.http.scaladsl.server.Directives.{
  complete,
  options,
  respondWithHeaders
}
import akka.http.scaladsl.server.{Directive0, Route}
import com.typesafe.config.Config

class DefaultCorsSupport(allowedOriginHost: String) extends CorsSupport {
  override val allowedOrigin: HttpOrigin =
    HttpOrigin(allowedOriginHost)

  override def addAccessControlHeaders: Directive0 = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`(allowedOrigin),
      `Access-Control-Allow-Credentials`(true),
      `Access-Control-Allow-Headers`("Authorization",
                                     "Content-Type",
                                     "X-Requested-With"),
      `Access-Control-Expose-Headers`("Set-Authorization", "Set-Refresh-Token")
    )
  }

  override def preflightRequestHandler: Route = options {
    complete(
      HttpResponse(StatusCodes.OK).withHeaders(
        `Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
  }

}

object DefaultCorsSupport {
  def fromConfig(config: Config): DefaultCorsSupport =
    new DefaultCorsSupport(config.getString("boot.allowed-origin"))
}
