import sbt.Keys._
import sbt._
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._

object Settings {

  lazy val commonSettings = Seq(
    libraryDependencies ++= Seq(
      Circe.core,
      Circe.generic,
      Circe.parser,
      Akka.http,
      Akka.stream,
      Akka.slf4j,
      Akka.contrib,
      Akka.`akka-http-crice`,
      Logback.classic,
      LogstashLogbackEncoder.encoder,
      Config.core,
      Kamon.datadog,
      Monix.version,
      MySQLConnectorJava.version,
      Redis.client
    ) ++ `doobie-quill`.all,
    scalafmtOnCompile in Compile := true,
    scalafmtTestOnCompile in Compile := true
  )

  lazy val packageSetting = Seq(
    organization := "com.github.BambooTuna",
    scalaVersion := "2.12.8",
    version := "1.0.0-SNAPSHOT",
    name := "AkkaServerSupport",
    publishTo := Some(Resolver.file("AkkaServerSupport",file("."))(Patterns(true, Resolver.mavenStyleBasePattern)))
  )

}
