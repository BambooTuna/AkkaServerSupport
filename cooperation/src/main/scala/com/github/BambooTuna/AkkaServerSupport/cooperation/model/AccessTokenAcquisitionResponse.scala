package com.github.BambooTuna.AkkaServerSupport.cooperation.model

trait AccessTokenAcquisitionResponse

case class AccessTokenAcquisitionResponseSuccess(
                                                  access_token: String,
                                                  token_type: String,
                                                  expires_in: Long,
                                                  refresh_token: String,
                                                  example_parameter: String
                                                ) extends AccessTokenAcquisitionResponse


case class AccessTokenAcquisitionResponseFailed(
                                                 error: String,
                                                 error_description: Option[String],
                                                 error_uri: Option[String]
                                               ) extends AccessTokenAcquisitionResponse
