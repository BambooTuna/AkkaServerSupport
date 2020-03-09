package com.github.BambooTuna.AkkaServerSupport.core.router

import akka.http.scaladsl.server.Route
import com.github.BambooTuna.AkkaServerSupport.core.error.{
  CustomExceptionHandler,
  CustomRejectionHandlers
}

trait RouteController
    extends CustomRejectionHandlers
    with CustomExceptionHandler {

  def toRoutes: Route

}
