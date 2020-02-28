package com.github.BambooTuna.AkkaServerSupport.core.session

import scala.util.Try

trait SessionStorageStrategy[K, V] {
  def store(key: K, value: V): Unit

  def find(key: K): Option[V]

  def remove(key: K): Unit
}
