package com.github.BambooTuna.AkkaServerSupport.authentication.model

case class LinkedUserCredentials(
    id: String,
    serviceId: String,
    serviceName: String,
    mail: Option[String]
)
