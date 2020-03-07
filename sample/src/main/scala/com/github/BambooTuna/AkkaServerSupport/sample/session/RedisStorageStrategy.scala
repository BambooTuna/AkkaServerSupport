package com.github.BambooTuna.AkkaServerSupport.sample.session

import com.github.BambooTuna.AkkaServerSupport.authentication.session.JWTSessionSettings
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import com.github.BambooTuna.AkkaServerSupport.sample.Main.system
import com.typesafe.config.Config
import redis.RedisClient

import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.duration._

class RedisStorageStrategy(dbSession: RedisClient)(
    implicit val executor: ExecutionContext,
    settings: JWTSessionSettings)
    extends StorageStrategy[String, String] {

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

object RedisStorageStrategy {
  def fromConfig(config: Config, name: String)(
      implicit executor: ExecutionContext,
      settings: JWTSessionSettings): RedisStorageStrategy = {
    val redisSession: RedisClient =
      RedisClient(
        host = config.getString(s"redis.${name}.host"),
        port = config.getInt(s"redis.${name}.port"),
        password =
          Some(config.getString(s"redis.${name}.password"))
            .filter(_.nonEmpty),
        db = Some(config.getInt(s"redis.${name}.db")),
        connectTimeout = Some(
          config
            .getDuration(s"redis.${name}.connect-timeout")
            .toMillis
            .millis)
      )
    new RedisStorageStrategy(redisSession)
  }
}
