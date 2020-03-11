package com.github.BambooTuna.AkkaServerSupport.sample.dao

import akka.actor.ActorSystem
import com.github.BambooTuna.AkkaServerSupport.authentication.session.JWTSessionSettings
import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import com.typesafe.config.Config
import redis.RedisClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class RedisStorageStrategy(dbSession: RedisClient)(
    implicit val executor: ExecutionContext,
    settings: JWTSessionSettings)
    extends StorageStrategy[String, String] {

  override def store(key: String, value: String): Future[Option[Unit]] =
    dbSession
      .set[String](key,
                   value,
                   exSeconds = Some(settings.expirationDate.toSeconds))
      .map(a => if (a) None else Some())

  override def find(key: String): Future[Option[String]] =
    dbSession
      .get[String](key)

  override def remove(key: String): Future[Option[Unit]] =
    dbSession
      .del(key)
      .map(a => if (a > 0) Some() else None)
}

object RedisStorageStrategy {
  def fromConfig(config: Config, name: String)(
      implicit system: ActorSystem,
      settings: JWTSessionSettings): RedisStorageStrategy = {
    implicit val executor: ExecutionContext = system.dispatcher
    val redisSession: RedisClient =
      RedisClient(
        host = config.getString(s"redis.${name}.host"),
        port = config.getInt(s"redis.${name}.port"),
        password = Some(config.getString(s"redis.${name}.password"))
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
