import sbt.Keys._
import sbt._
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport.{maintainer, packageName}
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptPlugin.autoImport.bashScriptDefines
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

object Settings {

  val sdk8 = "adoptopenjdk/openjdk8:x86_64-ubuntu-jdk8u212-b03-slim"

  lazy val commonSettings = Seq(
    libraryDependencies ++= Seq(
      Circe.core,
      Circe.generic,
      Circe.parser,
      Circe.shapes,
      Akka.http,
      Akka.stream,
      Akka.slf4j,
      Akka.contrib,
      Akka.`akka-http-crice`,
      Logback.classic,
      LogstashLogbackEncoder.encoder,
      Config.core,
      Monix.version
    ),
    scalafmtOnCompile in Compile := true,
    scalafmtTestOnCompile in Compile := true
  )

  lazy val dockerSettings = Seq(
    fork := true,
    name := "akkaserversupport",
    version := "latest",
    dockerBaseImage := sdk8,
    maintainer in Docker := "BambooTuna <bambootuna@gmail.com>",
    dockerUpdateLatest := true,
    dockerUsername := Some("bambootuna"),
    mainClass in (Compile, bashScriptDefines) := Some("com.github.BambooTuna.AkkaServerSupport.sample.Main"),
    packageName in Docker := name.value,
    dockerExposedPorts := Seq(8080)
  )

  lazy val packageSetting = (subName: String) =>
    Seq(
      organization := "com.github.BambooTuna",
      scalaVersion := "2.12.8",
      version := "1.1.0-SNAPSHOT",
      name := s"AkkaServerSupport$subName",
      publishTo := Some(Resolver.file("AkkaServerSupport",file("."))(Patterns(true, Resolver.mavenStyleBasePattern)))
    )

}
