package com.github.BambooTuna.AkkaServerSupport.authentication.router

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive, Route}
import akka.http.scaladsl.server.Directives._
import cats.Monad
import com.github.BambooTuna.AkkaServerSupport.authentication.json.SignInRequestJsonImpl
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCaseImpl
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.Future
import scala.util.{Failure, Success}
import io.circe.generic.auto._

trait AuthenticationRoute[IO[_], DBSession] extends FailFastCirceSupport {
  type QueryP[Q] = Directive[Q] => Route

  type UseCase = AuthenticationUseCaseImpl[IO, DBSession]
  val useCase: UseCase = new AuthenticationUseCaseImpl[IO, DBSession]

  def convertIO[O](flow: UseCase#IO[O]): Future[O]

  def signInRoute(dbSession: DBSession)(
      implicit F: Monad[UseCase#M]): QueryP[Unit] = _ {
    entity(as[SignInRequestJsonImpl]) { json: SignInRequestJsonImpl =>
      val f: UseCase#IO[Option[UseCase#U]] =
        useCase
          .signIn(json)(F)
          .value
          .run(dbSession)
      onComplete(convertIO[Option[UseCase#U]](f)) {
        case Success(Some(_)) => complete(StatusCodes.OK)
        case Success(None)    => complete(StatusCodes.Forbidden)
        case Failure(_)       => complete(StatusCodes.BadRequest)
      }
    }
  }

}
