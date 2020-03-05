package com.github.BambooTuna.AkkaServerSupport.sample

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase.AuthenticationUseCaseError

object ErrorHandling {

  def errorHandling(throwable: Throwable): StandardRoute = {
    throwable match {
      case e: RuntimeException =>
        complete(StatusCodes.InternalServerError, e.getMessage)
      case e: Exception =>
        complete(StatusCodes.BadRequest, e.getMessage)
    }
  }

  def customErrorHandler(error: AuthenticationUseCaseError): StandardRoute =
    error match {
      case AuthenticationUseCase.SignUpInsertError =>
        complete(StatusCodes.Conflict, "メールアドレスが使用されています")
      case AuthenticationUseCase.CantFoundUserError =>
        complete(StatusCodes.NotFound, "ユーザーが存在しません")
      case AuthenticationUseCase.SignInIdOrPassWrongError =>
        complete(StatusCodes.Forbidden, "認証失敗")
    }

}
