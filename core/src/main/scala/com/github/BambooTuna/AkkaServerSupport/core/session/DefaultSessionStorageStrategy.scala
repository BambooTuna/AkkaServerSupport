package com.github.BambooTuna.AkkaServerSupport.core.session
import com.github.BambooTuna.AkkaServerSupport.core.session.model.{
  RemoveTokenError,
  SessionStorageStrategy
}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

case class DefaultSessionStorageStrategy()
    extends SessionStorageStrategy[String, String] {

  val logger = LoggerFactory.getLogger(getClass)

  var storage: scala.collection.mutable.HashMap[String, String] =
    scala.collection.mutable.HashMap.empty

  def store(key: String, value: String): Future[Unit] = {
    logger.debug(s"store | key: $key, value: $value")
    storage.put(key, value)
    Future.successful(())
  }

  override def find(key: String): Future[Option[String]] = {
    val result =
      storage
        .get(key)
    result.foreach(a => logger.debug(s"success find | key: $key, value: $a"))
    Future.successful(result)
  }

  override def remove(key: String): Future[Unit] =
    storage
      .remove(key)
      .fold[Future[Unit]](
        Future.failed(RemoveTokenError)
      )(a => Future.successful(logger.debug(s"remove | key: $a")))

}
