package com.github.BambooTuna.AkkaServerSupport.authentication.router

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive, Route}
import akka.http.scaladsl.server.Directives._
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{SignInRequestJsonImpl, SignUpRequestJsonImpl}
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCaseImpl
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.Future
import scala.util.{Failure, Success}
import io.circe.generic.auto._

trait AuthenticationRouteImpl extends FailFastCirceSupport {
  type QueryP[Q] = Directive[Q] => Route

  val useCase: AuthenticationUseCaseImpl = new AuthenticationUseCaseImpl

  type IO[O] = useCase.userCredentialsDao.IO[O]
  type DBSession = useCase.userCredentialsDao.DBSession

  type M[O] = useCase.userCredentialsDao.M[O]
  type Id = useCase.userCredentialsDao.Id
  type SignInId = useCase.userCredentialsDao.SignInId
  type Record = useCase.userCredentialsDao.Record

  type SignUpRequest = useCase.SignUpRequest
  type SignInRequest = useCase.SignInRequest
  type PasswordInitializationRequest = useCase.PasswordInitializationRequest

  def convertIO[O](flow: IO[O]): Future[O]

  def signUpRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[SignUpRequestJsonImpl]) { json: SignUpRequestJsonImpl =>
      val f: IO[Record] =
        useCase
          .signUp(json)
          .run(dbSession)
      onComplete(convertIO[Record](f)) {
        case Success(value) => complete(StatusCodes.OK, s"${value.toString}")
        case Failure(_)       => complete(StatusCodes.BadRequest)
      }
    }
  }

  def signInRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[SignInRequestJsonImpl]) { json: SignInRequestJsonImpl =>
      val f: IO[Option[Record]] =
        useCase
          .signIn(json)
          .value
          .run(dbSession)
      onComplete(convertIO[Option[Record]](f)) {
        case Success(Some(value)) => complete(StatusCodes.OK, s"${value.toString}")
        case Success(None)    => complete(StatusCodes.Forbidden)
        case Failure(_)       => complete(StatusCodes.BadRequest)
      }
    }
  }

}