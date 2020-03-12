package com.github.BambooTuna.AkkaServerSupport.sample.useCase

import com.github.BambooTuna.AkkaServerSupport.authentication.dao.LinkedUserCredentialsDao
import com.github.BambooTuna.AkkaServerSupport.authentication.useCase.LinkedAuthenticationUseCase

class LinkedAuthenticationUseCaseImpl(
    val linkedUserCredentialsDao: LinkedUserCredentialsDao)
    extends LinkedAuthenticationUseCase
