package com.github.BambooTuna.AkkaServerSupport.core.session

import scala.concurrent.Future

trait StorageStrategy[K, V] {

  def store(key: K, value: V): Future[Unit]

  def find(key: K): Future[Option[V]]

  def remove(key: K): Future[Unit]

}
