package com.github.BambooTuna.AkkaServerSupport.cooperation.model

import com.github.BambooTuna.AkkaServerSupport.core.session.StorageStrategy
import com.typesafe.config.Config

case class OAuth2Settings(clientConfig: ClientConfig,
                          strategy: StorageStrategy[String, String])
