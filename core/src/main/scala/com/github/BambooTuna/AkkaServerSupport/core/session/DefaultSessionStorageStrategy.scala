package com.github.BambooTuna.AkkaServerSupport.core.session
import com.github.BambooTuna.AkkaServerSupport.core.session.model.SessionSerializer

case class DefaultSessionStorageStrategy[T](
    implicit ss: SessionSerializer[T, String])
    extends SessionStorageStrategy[String, T] {
  var storage: scala.collection.mutable.HashMap[String, String] =
    scala.collection.mutable.HashMap.empty

  override def store(key: String, value: T): Unit = {
    println(s"store | key: $key, value: $value")
    storage(key) = ss.serialize(value)
  }

  override def find(key: String): Option[T] = {
    for {
      v <- storage.get(key)
      r <- ss.deserialize(v).toOption
    } yield {
      println(s"find | key: $key, value: $r")
      r
    }
  }

  override def remove(key: String): Unit = {
    println(s"remove | key: $key")
    storage.remove(key)
  }
}
