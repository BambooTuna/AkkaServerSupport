# Authentication

## 動作確認

```bash
//サーバー起動
$ MYSQL_HOST=localhost sbt authentication/run

$ curl -X POST -H "Content-Type: application/json" -d '{"mail":"bambootuna@gmail.com","pass":"pass"}' localhost:18080/signup
$ curl -X POST -H "Content-Type: application/json" -d '{"signInId":"bambootuna@gmail.com","signInPass":"pass"}' localhost:18080/signin
```
※field nameを変更する

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
