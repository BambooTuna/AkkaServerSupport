package com.github.BambooTuna.AkkaServerSupport.core.error

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Rejection, RejectionHandler}

trait CustomRejectionHandlers {

  final protected val defaultRejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case e: Rejection =>
          complete(StatusCodes.BadRequest, e.getClass.getName)
      }
      .result()

  /*
    Rejection Sample
    sealed abstract class CustomRejection(val code: String) extends Rejection {
      def message: String
    }

    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
    import io.circe.generic.auto._
    case class ErrorResponseJson(error_code: String, message: String)
    complete(StatusCodes.BadRequest -> ErrorResponseJson("", ""))
 */

}
