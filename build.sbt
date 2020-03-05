import Settings._

lazy val core = (project in file("core"))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      JWT.core,
      JWT.circe
    )
  )

lazy val authentication = (project in file("authentication"))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(commonSettings)
  .dependsOn(core)

lazy val cooperation = (project in file("cooperation"))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(commonSettings)
  .dependsOn(authentication)

lazy val sample = (project in file("sample"))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(commonSettings)
  .settings(dockerSettings)
  .settings(
    libraryDependencies ++= Seq(
      MySQLConnectorJava.version,
      Redis.client
    ) ++ `doobie-quill`.all
  )
  .dependsOn(cooperation)

lazy val root =
  (project in file("."))
    .aggregate(core, authentication, cooperation, sample)
