package com.github.BambooTuna.AkkaServerSupport.sample

import java.time.ZoneId
import java.time.format.DateTimeFormatter;

object SystemSettings {

  def now() = java.time.Instant.now().atZone(timeZone)

  val timeZone = ZoneId.of("Asia/Tokyo")

  val timeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")

  def generateId(): String =
    java.util.UUID.randomUUID.toString.replaceAll("-", "")

  def generatePassword(): String =
    java.util.UUID.randomUUID.toString.replaceAll("-", "")

}
