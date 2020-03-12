package com.github.BambooTuna.AkkaServerSupport.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, POST, PUT}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.github.BambooTuna.AkkaServerSupport.core.router.{
  RouteController,
  Router,
  route
}
import monix.execution.Scheduler
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

class RouteControllerImpl(implicit system: ActorSystem,
                          mat: Materializer,
                          executor: ExecutionContext)
    extends Component
    with RouteController {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  override def toRoutes: Route =
    handleExceptions(defaultExceptionHandler(logger)) {
      handleRejections(defaultRejectionHandler) {
        (
          authenticationCodeCycleRoute(
            monix.execution.Scheduler.Implicits.global) +
            accountCycleRoute(monix.execution.Scheduler.Implicits.global) +
            oauth2Route(monix.execution.Scheduler.Implicits.global)
        ).create
      }
    }

  def authenticationCodeCycleRoute(implicit s: Scheduler): Router = {
    Router(
      route(PUT, "activate", authenticationController.issueActivateCodeRoute),
      route(GET,
            "activate" / Segment,
            authenticationController.activateAccountRoute),
      route(POST, "init", authenticationController.tryInitializationRoute),
      route(GET, "init" / Segment, authenticationController.initAccountPassword)
    )
  }

  def accountCycleRoute(implicit s: Scheduler): Router =
    Router(
      route(POST, "signup", authenticationController.signUpRoute),
      route(POST, "signin", authenticationController.signInRoute),
      route(GET, "health", authenticationController.healthCheck),
      route(DELETE, "logout", authenticationController.logout)
    )

  def oauth2Route(implicit s: Scheduler): Router =
    Router(
      route(POST,
            "oauth2" / "signin" / "line",
            lineOAuth2Controller.fetchCooperationLink),
      route(GET,
            "oauth2" / "signin" / "line",
            lineOAuth2Controller.authenticationFromCode)
    )

}
