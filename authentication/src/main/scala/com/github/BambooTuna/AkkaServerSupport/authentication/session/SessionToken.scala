package com.github.BambooTuna.AkkaServerSupport.authentication.session

import com.github.BambooTuna.AkkaServerSupport.core.session.{
  SessionSerializer,
  StringSessionSerializer
}
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

case class SessionToken(userId: String, cooperation: Option[String] = None)

object SessionToken {
  implicit def serializer: SessionSerializer[SessionToken, String] =
    new StringSessionSerializer(
      _.asJson.noSpaces,
      (in: String) => parser.decode[SessionToken](in).toTry)
}
