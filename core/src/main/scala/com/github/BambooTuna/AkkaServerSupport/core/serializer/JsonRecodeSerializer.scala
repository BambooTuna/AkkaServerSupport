package com.github.BambooTuna.AkkaServerSupport.core.serializer

trait JsonRecodeSerializer[J, R] {

  def toRecode(json: J): R

}
