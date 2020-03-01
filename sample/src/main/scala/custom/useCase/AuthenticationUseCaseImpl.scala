package custom.useCase

import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.AuthenticationUseCase
import custom.dao.UserCredentialsDaoImpl
import custom.json.{
  PasswordInitializationRequestJsonImpl,
  SignInRequestJsonImpl,
  SignUpRequestJsonImpl
}
import custom.model.UserCredentialsImpl

class AuthenticationUseCaseImpl extends AuthenticationUseCase {
  override type Record = UserCredentialsImpl

  override type SignUpRequest = SignUpRequestJsonImpl
  override type SignInRequest = SignInRequestJsonImpl
  override type PasswordInitializationRequest =
    PasswordInitializationRequestJsonImpl

  override val userCredentialsDao: UserCredentialsDaoImpl =
    new UserCredentialsDaoImpl

  override def ioErrorHandling[T, U >: T](io: IO[T], f: Throwable => U): IO[U] =
    io.onErrorHandle(f)

}
