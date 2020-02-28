## セッション
### 動作確認
```bash
$ curl -X GET http://localhost:8080/set/token
$ curl -X POST http://localhost:8080/login -H "Set-Auth: token"
$ curl -X DELETE http://localhost:8080/logout -H "Set-Auth: token"
```
