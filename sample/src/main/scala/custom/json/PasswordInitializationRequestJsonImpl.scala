package custom.json

import com.github.BambooTuna.AkkaServerSupport.authentication.json.PasswordInitializationRequestJson
import custom.model.UserCredentialsImpl

case class PasswordInitializationRequestJsonImpl(mail: String)
    extends PasswordInitializationRequestJson[UserCredentialsImpl] {
  override val signInId: String = mail
}
