# Authentication

## 使い方
### 実装すべきクラス

#### モデル
[UserCredentialsImpl](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/model/UserCredentialsImpl.scala)
[EncryptedPasswordImpl](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/model/EncryptedPasswordImpl.scala)

#### リクエストJson
[SignUpRequestJsonImpl](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/json/SignUpRequestJsonImpl.scala)
[SignInRequestJsonImpl](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/json/SignInRequestJsonImpl.scala)
[PasswordInitializationRequestJsonImpl](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/json/PasswordInitializationRequestJsonImpl.scala)

#### ユースケース
[AuthenticationUseCaseImpl](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/useCase/AuthenticationUseCaseImpl.scala)
基本のメソッド以外に追加してもよい

#### ルーター
[AuthenticationRouteImpl](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/router/AuthenticationRouteImpl.scala)

#### Dao
[UserCredentialsDaoImpl](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/dao/UserCredentialsDaoImpl.scala)
ユーザーの認証情報用のDao

[RedisSessionStorageStrategy](https://github.com/BambooTuna/AkkaServerSupport/blob/master/sample/src/main/scala/com/github/BambooTuna/AkkaServerSupport/sample/session/RedisSessionStorageStrategy.scala)
セッション情報を保持していくところ、サンプルではRedisを使用
※インメモリー: InMemoryStorageStrategy.scala


## 動作確認

1. Build
`$ sbt sample/docker:publishLocal`

2. サーバー起動
`docker-compose.yml`の環境変数に値をセットする
- `LINE_CLIENT_ID`
- `LINE_CLIENT_SECRET`

```bash
$ docker-compose up --build
```

### Redis操作
```bash
$ docker-compose exec redis sh

/data # redis-cli

//データーベース指定(1番を指定)
127.0.0.1:6379> SELECT 1 

// key一覧
127.0.0.1:6379[1]> keys *

//value取得
127.0.0.1:6379[1]> get [key]

//有効期限取得
127.0.0.1:6379[1]> ttl [key] 
```

### SignUp, SignIn
```bash
$ curl -X POST -H "Content-Type: application/json" -d '{"mail":"bambootuna@gmail.com","pass":"pass"}' localhost:8080/signup -i
$ curl -X POST -H "Content-Type: application/json" -d '{"mail":"bambootuna@gmail.com","pass":"pass"}' localhost:8080/signin -i

$ export SESSION_TOKEN=~~~
```
HeaderName=`Set-Authorization`にセッショントークンがセットされた状態でレスポンスが帰ってくるのでそれを使う

### アカウント有効化メール送信
```bash
$ curl -X PUT -H "Authorization: $SESSION_TOKEN" localhost:8080/activate -i
```
### HealthCheck
セッショントークンが有効か確認する
HeaderName=`Authorization`にセッショントークンをセットする
```bash
$ curl -X GET localhost:8080/health -H "Authorization: $SESSION_TOKEN"
```

### LogOut
```bash
$ curl -X DELETE localhost:8080/logout -H "Authorization: $SESSION_TOKEN"
```

### パスワード初期化
```bash
$ curl -X POST -H "Content-Type: application/json" -d '{"mail":"bambootuna@gmail.com"}' localhost:8080/init -i
```

### SNS連携
- Line連携のリンク発行
```bash
$ curl -X POST http://localhost:8080/oauth2/signin/line
{"redirectUri":"~~~"}
```

- 連携承認後
`http://localhost:8080/oauth2/signin/line`にリダイレクト
HeaderName=`Set-Authorization`にセッショントークンがセットされる

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
      _.id -> "id",
      _.signinId -> "mail",
      _.signinPass.encryptedPass -> "pass"
    )
```
