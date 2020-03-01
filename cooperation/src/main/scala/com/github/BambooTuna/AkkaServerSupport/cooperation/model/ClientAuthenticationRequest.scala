package com.github.BambooTuna.AkkaServerSupport.cooperation.model

trait ClientAuthenticationRequest {
  val client_id: String
  val scope: Option[String]
  val state: Option[String]
  val redirect_uri: Option[String]
  val response_type: String//サーバーサイドの認証の場合は`code`を指定。その場合、認可リクエストを承認すると認可コードが返却される。
  def generateState: String = java.util.UUID.randomUUID.toString.replaceAll("-", "")
}
