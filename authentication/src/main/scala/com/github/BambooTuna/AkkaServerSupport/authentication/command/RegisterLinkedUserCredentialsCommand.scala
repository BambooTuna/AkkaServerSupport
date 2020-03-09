package com.github.BambooTuna.AkkaServerSupport.authentication.command

import com.github.BambooTuna.AkkaServerSupport.authentication.model.LinkedUserCredentials
import com.github.BambooTuna.AkkaServerSupport.core.serializer.JsonRecodeSerializer

case class RegisterLinkedUserCredentialsCommand(serviceId: String,
                                                serviceName: String)

object RegisterLinkedUserCredentialsCommand {
  implicit val rs =
    new JsonRecodeSerializer[RegisterLinkedUserCredentialsCommand,
                             LinkedUserCredentials] {
      override def toRecode(
          json: RegisterLinkedUserCredentialsCommand): LinkedUserCredentials =
        LinkedUserCredentials(
          id = java.util.UUID.randomUUID.toString.replaceAll("-", ""),
          serviceId = json.serviceId,
          serviceName = json.serviceName,
          mail = None
        )
    }
}
