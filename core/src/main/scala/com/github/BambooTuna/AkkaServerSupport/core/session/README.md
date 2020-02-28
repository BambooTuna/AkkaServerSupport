## セッション
### 動作確認
```bash
$ curl -X GET http://localhost:8080/set/token -i
Authorization: token

$ export AUTH_TOEKN=

$ curl -X POST http://localhost:8080/login -H "Set-Authorization: $AUTH_TOEKN"
$ curl -X DELETE http://localhost:8080/logout -H "Set-Authorization: $AUTH_TOEKN"
```
