package custom.json

import com.github.BambooTuna.AkkaServerSupport.authentication.json.SignUpRequestJson
import custom.{SystemSettings, model}
import custom.model.{EncryptedPasswordImpl, UserCredentialsImpl}

case class SignUpRequestJsonImpl(mail: String, pass: String)
    extends SignUpRequestJson[UserCredentialsImpl] {
  override def createUserCredentials: UserCredentialsImpl =
    model.UserCredentialsImpl(
      id = SystemSettings.generateId(),
      signinId = mail,
      signinPass = EncryptedPasswordImpl(pass).changeEncryptedPass(pass))
}
