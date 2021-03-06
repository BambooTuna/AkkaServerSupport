package com.github.BambooTuna.AkkaServerSupport.core.session

import scala.util.Try

trait SessionSerializer[L, R] {
  def serialize(t: L): R
  def deserialize(r: R): Try[L]
}
