package com.github.BambooTuna.AkkaServerSupport.authentication.dao

import com.github.BambooTuna.AkkaServerSupport.core.session.DefaultSessionSettings
import com.github.BambooTuna.AkkaServerSupport.core.session.model.SessionStorageStrategy
import redis.RedisClient

import scala.concurrent.{ExecutionContext, Future}

class RedisSessionStorageStrategy(dbSession: RedisClient)(
    implicit executor: ExecutionContext,
    settings: DefaultSessionSettings)
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
      .filter(_ > 0)
      .map(_ => ())

}
