package com.github.BambooTuna.AkkaServerSupport.core.error

import akka.http.scaladsl.server.StandardRoute

trait ErrorHandleSupport {

  def fromThrowable(throwable: Throwable): StandardRoute

}
