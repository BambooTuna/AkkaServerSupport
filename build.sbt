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
  .settings(dockerSettings)
  .dependsOn(core)

lazy val sample = (project in file("sample"))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(commonSettings)
  .settings(dockerSettings)
  .dependsOn(authentication)

lazy val root =
  (project in file("."))
    .aggregate(core, authentication, sample)
