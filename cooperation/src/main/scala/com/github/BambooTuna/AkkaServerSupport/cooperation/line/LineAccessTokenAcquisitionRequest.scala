package com.github.BambooTuna.AkkaServerSupport.cooperation.line

import com.github.BambooTuna.AkkaServerSupport.authentication.oauth2.AccessTokenAcquisitionRequest
import io.circe._
import io.circe.Json._

case class LineAccessTokenAcquisitionRequest(grant_type: String,
                                             code: String,
                                             redirect_uri: String,
                                             client_id: String,
                                             client_secret: String)
    extends AccessTokenAcquisitionRequest

object LineAccessTokenAcquisitionRequest {
  implicit val encoder: Encoder[LineAccessTokenAcquisitionRequest] =
    (p: LineAccessTokenAcquisitionRequest) => {
      obj(
        "grant_type" -> fromString(p.grant_type),
        "code" -> fromString(p.code),
        "redirect_uri" -> fromString(p.redirect_uri),
        "client_id" -> fromString(p.client_id),
        "client_secret" -> fromString(p.client_secret),
      )
    }
}
