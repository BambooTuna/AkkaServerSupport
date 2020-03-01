package custom.json

import com.github.BambooTuna.AkkaServerSupport.authentication.json.SignInRequestJson
import custom.model.UserCredentialsImpl

case class SignInRequestJsonImpl(mail: String, pass: String)
    extends SignInRequestJson[UserCredentialsImpl] {
  override val signInId: String = mail
  override val signInPass: String = pass
}
