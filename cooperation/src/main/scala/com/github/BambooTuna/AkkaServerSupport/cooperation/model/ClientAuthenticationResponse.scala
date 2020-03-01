package com.github.BambooTuna.AkkaServerSupport.cooperation.model

import scala.util.Try

trait ClientAuthenticationResponse {
  val state: Option[String]
}

case class ClientAuthenticationResponseSuccess(code: String, state: Option[String]) extends ClientAuthenticationResponse {
  def createAccessTokenAcquisitionRequest(r: ClientAuthenticationRequest): Try[AccessTokenAcquisitionRequest] =
    Try {
      require(this.state == r.state, "CSRF Token is not equal")
      AccessTokenAcquisitionRequest(
        code = this.code,
        redirect_uri = r.redirect_uri,
        //TODO -> client_id: 認可サーバによってクライアントが認証されていない場合は必須です。とあるがどういうこと？
        client_id = Some(r.client_id)
      )
    }
}

case class ClientAuthenticationResponseFailed(
                                               error: String,
                                               error_description: Option[String],
                                               error_uri: Option[String],
                                               state: Option[String]
                                             ) extends ClientAuthenticationResponse
