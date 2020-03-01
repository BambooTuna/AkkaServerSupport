package custom.model

import com.github.BambooTuna.AkkaServerSupport.authentication.json.PasswordInitializationRequestJson
import com.github.BambooTuna.AkkaServerSupport.authentication.model.UserCredentials
import custom.SystemSettings

case class UserCredentialsImpl(id: String,
                               signinId: String,
                               signinPass: EncryptedPasswordImpl)
    extends UserCredentials {
  override type Id = String
  override type SigninId = String
  override type SigninPass = EncryptedPasswordImpl

  override def doAuthenticationByPassword(inputPass: Any): Boolean =
    signinPass == inputPass

  //TODO
  override def initializeAuthentication(
      json: PasswordInitializationRequestJson[_]): Boolean = false

  override def changePassword(newPlainPassword: String): UserCredentialsImpl =
    copy(signinPass = signinPass.changeEncryptedPass(newPlainPassword))

  override def initPassword(): (UserCredentialsImpl, String) = {
    val newPlainPassword = SystemSettings.generateId()
    (changePassword(newPlainPassword), newPlainPassword)
  }

}
