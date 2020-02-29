package com.github.BambooTuna.AkkaServerSupport.authentication.session

import com.github.BambooTuna.AkkaServerSupport.core.session.SessionStorageStrategy
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class DefaultSessionStorageStrategy(implicit val executor: ExecutionContext)
    extends SessionStorageStrategy[String, String] {

  val logger = LoggerFactory.getLogger(getClass)

  var storage: scala.collection.mutable.HashMap[String, String] =
    scala.collection.mutable.HashMap.empty

  def store(key: String, value: String): Future[Unit] =
    Future { storage.put(key, value) }

  override def find(key: String): Future[Option[String]] =
    Future { storage.get(key) }

  override def remove(key: String): Future[Unit] =
    Future { storage.remove(key).get }

}
