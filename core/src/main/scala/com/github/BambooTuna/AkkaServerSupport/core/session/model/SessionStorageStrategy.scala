package com.github.BambooTuna.AkkaServerSupport.core.session.model

import scala.concurrent.Future

trait SessionStorageStrategy[K, V] {
  def store(key: K, value: V): Future[Unit]

  def find(key: K): Future[Option[V]]

  def remove(key: K): Future[Unit]
}

trait SessionStorageStrategyError
object StoreTokenError extends Exception("StoreTokenError")
object RemoveTokenError extends Exception("RemoveTokenError")
