# AkkaServerSupport
Akkaでサーバーを立てるときによく使う部分もモジュール化したい

## セッションを使ったユーザー管理
[README.md](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/README.md)

## 依存
```
resolvers += "Maven Repo on github" at "https://BambooTuna.github.io/AkkaServerSupport",
libraryDependencies ++= Seq(
  "com.github.BambooTuna" %% "akkaserversupport-core" % "1.1.0-SNAPSHOT",
  "com.github.BambooTuna" %% "akkaserversupport-authentication" % "1.1.0-SNAPSHOT",
  "com.github.BambooTuna" %% "akkaserversupport-cooperation" % "1.1.0-SNAPSHOT"
)
```

## Build
```bash
$ sbt core/publish
$ sbt authentication/publish
$ sbt cooperation/publish

sbt
core/publish
authentication/publish
cooperation/publish
```
