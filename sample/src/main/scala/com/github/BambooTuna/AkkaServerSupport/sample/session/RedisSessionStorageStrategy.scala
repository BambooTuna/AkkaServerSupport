package com.github.BambooTuna.AkkaServerSupport.sample.session

import com.github.BambooTuna.AkkaServerSupport.authentication.session.JWTSessionSettings
import com.github.BambooTuna.AkkaServerSupport.core.session.SessionStorageStrategy
import redis.RedisClient

import scala.concurrent.{ExecutionContext, Future}

class RedisSessionStorageStrategy(dbSession: RedisClient)(
    implicit val executor: ExecutionContext,
    settings: JWTSessionSettings)
    extends SessionStorageStrategy[String, String] {

  override def store(key: String, value: String): Future[Unit] =
    dbSession
      .set[String](key,
                   value,
                   exSeconds = Some(settings.expirationDate.toSeconds))
      .filter(identity)
      .map(_ => ())

  override def find(key: String): Future[Option[String]] =
    dbSession
      .get[String](key)

  override def remove(key: String): Future[Unit] =
    dbSession
      .del(key)
      .map(_ => ())

}
