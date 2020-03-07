package com.github.BambooTuna.AkkaServerSupport.core.router

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}

trait CorsSupport {
  val allowedOrigin: HttpOrigin

  protected def addAccessControlHeaders: Directive0

  protected def preflightRequestHandler: Route

  def corsHandler(r: Route): Route = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }
}
