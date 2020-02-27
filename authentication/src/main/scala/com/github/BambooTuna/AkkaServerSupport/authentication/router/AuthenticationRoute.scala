//package com.github.BambooTuna.AkkaServerSupport.authentication.router
//
//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.server.Directives._
//import cats.Monad
//import cats.data.{EitherT, OptionT}
//import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
//  SignInRequestJson,
//  SignUpRequestJson
//}
//import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
//import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
//
//import scala.concurrent.Future
//import scala.util.{Failure, Success}
//
//import io.circe.syntax._
//import io.circe.generic.auto._
//
//trait AuthenticationRoute extends FailFastCirceSupport {
//  type UseCase <: AuthenticationUseCase
//  protected type DBSession = UseCase#DBSession
//  val useCase: UseCase
//
//  def convertIO[O](flow: UseCase#IO[O]): Future[O]
//  def convertIO[O](flow: OptionT[UseCase#IO, O]): Future[Option[O]]
//
//  def signInRoute(dbSession: DBSession)(implicit F: Monad[UseCase#M]) = {
//    entity(as[SignInRequestJson[UseCase#U]]) {
//      json: SignInRequestJson[useCase.U] =>
//        val f: OptionT[UseCase#IO, UseCase#U] =
//          useCase
//            .signIn(json)
//            .value
//            .run(dbSession)
//        onComplete(convertIO[UseCase#U](f)) {
//          case Success(Some(_)) => complete(StatusCodes.OK)
//          case Success(None)    => complete(StatusCodes.Forbidden)
//          case Failure(_)       => complete(StatusCodes.BadRequest)
//        }
//    }
//  }
//
//}
