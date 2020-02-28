package com.github.BambooTuna.AkkaServerSupport.core.session.model
import scala.util.Try

case class StringSessionSerializer[T](toValue: T => String,
                                      fromValue: String => Try[T])
    extends SessionSerializer[T, String] {
  override def serialize(t: T): String = toValue(t)
  override def deserialize(r: String): Try[T] = fromValue(r)
}
