package com.github.BambooTuna.AkkaServerSupport.core.session

import scala.concurrent.Future

trait StorageStrategy[K, V] {

  //すでにkeyにvalueがセットされている場合はSome(), ない場合はNone
  def store(key: K, value: V): Future[Option[Unit]]

  def find(key: K): Future[Option[V]]

  def remove(key: K): Future[Option[Unit]]

}
