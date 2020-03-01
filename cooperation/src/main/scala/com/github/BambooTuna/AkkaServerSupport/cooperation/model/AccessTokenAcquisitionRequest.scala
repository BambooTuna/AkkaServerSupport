package com.github.BambooTuna.AkkaServerSupport.cooperation.model

case class AccessTokenAcquisitionRequest(
                                          grant_type: String = "authorization_code",
                                          code: String,
                                          redirect_uri: Option[String],
                                          client_id: Option[String]
                                        )
