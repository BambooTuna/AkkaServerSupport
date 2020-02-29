package com.github.BambooTuna.AkkaServerSupport.authentication.router

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive, Route}
import akka.http.scaladsl.server.Directives._
import com.github.BambooTuna.AkkaServerSupport.authentication.json.{
  PasswordInitializationRequestJsonImpl,
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import com.github.BambooTuna.AkkaServerSupport.authentication.router.AuthenticationRouteImpl.SessionToken
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCaseImpl
import com.github.BambooTuna.AkkaServerSupport.core.session.{
  DefaultSession,
  DefaultSessionSettings,
  DefaultSessionStorageStrategy
}
import com.github.BambooTuna.AkkaServerSupport.core.session.model.{
  SessionSerializer,
  SessionStorageStrategy,
  StringSessionSerializer
}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

trait AuthenticationRouteImpl extends FailFastCirceSupport {
  type QueryP[Q] = Directive[Q] => Route

  implicit val executor: ExecutionContext
  implicit val settings: DefaultSessionSettings
  implicit val strategy: SessionStorageStrategy[String, String]

  implicit def serializer: SessionSerializer[SessionToken, String] =
    new StringSessionSerializer(
      _.asJson.noSpaces,
      (in: String) => parser.decode[SessionToken](in).toTry)
  protected val session: DefaultSession[SessionToken] =
    new DefaultSession[SessionToken](settings)

  val useCase: AuthenticationUseCaseImpl = new AuthenticationUseCaseImpl

  type IO[O] = useCase.userCredentialsDao.IO[O]
  type DBSession = useCase.userCredentialsDao.DBSession

  type M[O] = useCase.userCredentialsDao.M[O]
  type Id = useCase.userCredentialsDao.Id
  type Record = useCase.userCredentialsDao.Record

  type SignUpRequest = useCase.SignUpRequest
  type SignInRequest = useCase.SignInRequest
  type PasswordInitializationRequest = useCase.PasswordInitializationRequest

  def convertIO[O](flow: IO[O]): Future[O]

  def signUpRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[SignUpRequestJsonImpl]) { json: SignUpRequestJsonImpl =>
      val f: IO[Option[Record]] =
        useCase
          .signUp(json)
          .value
          .run(dbSession)
      onComplete(convertIO[Option[Record]](f)) {
        case Success(Some(value)) =>
          session.setSession(SessionToken(value.id)) {
            complete(StatusCodes.OK)
          }
        case Success(None) => complete(StatusCodes.Forbidden)
        case Failure(_)    => complete(StatusCodes.BadRequest)
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
        case Success(Some(value)) =>
          session.setSession(SessionToken(value.id)) {
            complete(StatusCodes.OK)
          }
        case Success(None) => complete(StatusCodes.Forbidden)
        case Failure(_)    => complete(StatusCodes.BadRequest)
      }
    }
  }

  def passwordInitializationRoute(dbSession: DBSession): QueryP[Unit] = _ {
    entity(as[PasswordInitializationRequestJsonImpl]) {
      json: PasswordInitializationRequestJsonImpl =>
        val f: IO[Option[String]] =
          useCase
            .passwordInitialization(json)
            .value
            .run(dbSession)
        onComplete(convertIO[Option[String]](f)) {
          case Success(Some(value)) =>
            complete(
              StatusCodes.OK,
              s"""{"send_new_pass_to":"${json.mail}","message":"After signin, please change your password!"}""")
          case Success(None) => complete(StatusCodes.Forbidden)
          case Failure(_)    => complete(StatusCodes.BadRequest)
        }
    }
  }

  def healthCheck: QueryP[Unit] = _ {
    session.requiredSession { sessionToken =>
      complete(
        StatusCodes.OK,
        s"""Internal DEBUG: sessionToken.userId is ${sessionToken.userId}""")
    }
  }

}

object AuthenticationRouteImpl {
  case class SessionToken(userId: String)
}
