package com.github.BambooTuna.AkkaServerSupport.sample.useCase

import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.LinkedAuthenticationUseCase
import com.github.BambooTuna.AkkaServerSupport.sample.dao.LinkedUserCredentialsDaoImpl

class LinkedAuthenticationUseCaseImpl extends LinkedAuthenticationUseCase {
  override val linkedUserCredentialsDao: LinkedUserCredentialsDaoImpl =
    new LinkedUserCredentialsDaoImpl
}
