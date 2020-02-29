package com.github.BambooTuna.AkkaServerSupport.core.session

import com.github.BambooTuna.AkkaServerSupport.core.session.SessionStorageStrategy.{
  StrategyFindError,
  StrategyRemoveError,
  StrategyStoreError
}

import scala.concurrent.{ExecutionContext, Future}

trait SessionStorageStrategy[K, V] {
  implicit val executor: ExecutionContext

  protected def store(key: K, value: V): Future[Unit]

  protected def find(key: K): Future[Option[V]]

  protected def remove(key: K): Future[Unit]

  final def runStore(key: K, value: V): Future[Unit] =
    store(key, value).recover {
      case e: Throwable => throw StrategyStoreError(e.getMessage)
    }

  final def runFind(key: K): Future[Option[V]] =
    find(key).recover {
      case e: Throwable => throw StrategyFindError(e.getMessage)
    }

  final def runRemove(key: K): Future[Unit] =
    remove(key).recover {
      case e: Throwable => throw StrategyRemoveError(e.getMessage)
    }

}

object SessionStorageStrategy {
  sealed class SessionStorageStrategyError(message: String)
      extends RuntimeException(message)
  case class StrategyStoreError(message: String)
      extends SessionStorageStrategyError(message)
  case class StrategyFindError(message: String)
      extends SessionStorageStrategyError(message)
  case class StrategyRemoveError(message: String)
      extends SessionStorageStrategyError(message)
}
