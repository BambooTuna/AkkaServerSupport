package com.github.BambooTuna.AkkaServerSupport.authentication.oauth2

import akka.http.scaladsl.model.Uri
import com.typesafe.config.Config

case class ClientConfig(
    serviceName: String,
    clientId: String,
    clientSecret: String,
    redirectUri: Uri,
    authenticationCodeIssuanceUri: Uri,
    accessTokenIssuanceUri: Uri
)

object ClientConfig {
  def fromConfig(serviceName: String, config: Config): ClientConfig =
    ClientConfig(
      serviceName = serviceName,
      clientId = config.getString(s"oauth2.${serviceName}.client_id"),
      clientSecret = config.getString(s"oauth2.${serviceName}.client_secret"),
      redirectUri = Uri(config.getString(s"oauth2.${serviceName}.redirect_uri")),
      authenticationCodeIssuanceUri = Uri(
        config.getString(
          s"oauth2.${serviceName}.authentication_code_issuance_uri")),
      accessTokenIssuanceUri = Uri(
        config.getString(s"oauth2.${serviceName}.access_token_issuance_uri"))
    )
}
