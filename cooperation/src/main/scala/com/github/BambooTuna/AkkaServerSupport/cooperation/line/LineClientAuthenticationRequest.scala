package com.github.BambooTuna.AkkaServerSupport.cooperation.line

import com.github.BambooTuna.AkkaServerSupport.cooperation.model.ClientAuthenticationRequest
import io.circe.Encoder
import io.circe.Json._

case class LineClientAuthenticationRequest(
    response_type: String,
    client_id: String,
    redirect_uri: String,
    state: String,
    scope: Option[String],
    nonce: Option[String],
    prompt: Option[String],
    max_age: Option[Int],
    ui_locales: Option[String],
    bot_prompt: Option[String]
) extends ClientAuthenticationRequest

object LineClientAuthenticationRequest {
  implicit val encoder: Encoder[LineClientAuthenticationRequest] =
    (p: LineClientAuthenticationRequest) => {
      obj(
        "response_type" -> fromString(p.response_type),
        "client_id" -> fromString(p.client_id),
        "redirect_uri" -> fromString(p.redirect_uri),
        "state" -> fromString(p.state),
        "scope" -> p.scope.fold(Null)(fromString),
        "nonce" -> p.nonce.fold(Null)(fromString),
        "prompt" -> p.prompt.fold(Null)(fromString),
        "max_age" -> p.max_age.fold(Null)(fromInt),
        "ui_locales" -> p.ui_locales.fold(Null)(fromString),
        "bot_prompt" -> p.bot_prompt.fold(Null)(fromString)
      )
    }
}
