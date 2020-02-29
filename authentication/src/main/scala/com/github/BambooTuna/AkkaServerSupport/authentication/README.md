# Authentication

```bash
$ sbt authentication/docker:publishLocal
```
## 動作確認

```bash
//サーバー起動
$ MYSQL_HOST=localhost sbt authentication/run

$ curl -X POST -H "Content-Type: application/json" -d '{"mail":"bambootuna@gmail.com","pass":"pass"}' localhost:8080/signup -i
$ curl -X POST -H "Content-Type: application/json" -d '{"mail":"bambootuna@gmail.com","pass":"pass"}' localhost:8080/signin -i

$ curl -X GET localhost:8080/health -H "Set-Authorization: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1ODMwNjM1MjcsImlhdCI6MTU4Mjk3NzEyNywianRpIjoiYzhmZmQ1MmZhNzgxNDgxMmI5ZTVjOTg3ZjlmODVkYTMiLCJ1c2VySWQiOiJkNDc2MDk3OWQyMjc0NTIzODkwYTI0MTI4MzdkZmI3ZSJ9.8wuWt5bhZcglsUohSQquMehDEN6j8aFvuRJNwraNHic"
```

## データーベース構成
...


## 注意
getquill
Daoで畳み込みのあるケースクラスを使うとき
1. 子クラスに`io.getquill.Embedded`を継承する

2. マッピング用の暗黙を追加
```scala
implicit lazy val daoSchemaMeta =
    schemaMeta[Record](
      "user_credentials",
      _.signInId -> "id",
      _.signInPass.encryptedPass -> "pass"
    )
```
