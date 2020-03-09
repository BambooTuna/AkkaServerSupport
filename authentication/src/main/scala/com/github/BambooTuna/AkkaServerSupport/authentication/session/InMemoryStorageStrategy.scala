package com.github.BambooTuna.AkkaServerSupport.authentication.session

import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class InMemoryStorageStrategy(implicit val executor: ExecutionContext)
    extends StorageStrategy[String, String] {

  val logger = LoggerFactory.getLogger(getClass)

  var storage: scala.collection.mutable.HashMap[String, String] =
    scala.collection.mutable.HashMap.empty

  def store(key: String, value: String): Future[Option[Unit]] =
    Future { storage.put(key, value).map(_ => ()) }

  override def find(key: String): Future[Option[String]] =
    Future { storage.get(key) }

  override def remove(key: String): Future[Option[Unit]] =
    Future { storage.remove(key).map(_ => ()) }

}
