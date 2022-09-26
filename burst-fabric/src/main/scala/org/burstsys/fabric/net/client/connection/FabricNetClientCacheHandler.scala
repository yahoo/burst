/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client.connection

import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.net.message.cache._
import org.burstsys.tesla.thread.request._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * dispatch cache operations to client connection
 */
trait FabricNetClientCacheHandler {

  self: FabricNetClientConnectionContext =>

  /**
   * Respond to a request from the supervisor to perform a cache operation.
   */
  final
  def cacheManageOperation(msg: FabricNetCacheOperationReqMsg): Unit = {
    val resultPromise = Promise[Unit]()

    def sendResponse(generations: Array[FabricGeneration]): Future[Unit] = {
      val responsePromise = Promise[Unit]()
      transmitter transmitControlMessage FabricNetCacheOperationRespMsg(msg, clientKey, serverKey, generations) onComplete {
        case Failure(t) => responsePromise.failure(t)
        case Success(_) => responsePromise.success((): Unit)
      }
      responsePromise.future
    }

    if (client.cache == null) {
      sendResponse(Array.empty) onComplete {
        case Failure(t) => resultPromise.failure(t)
        case Success(()) => resultPromise.success((): Unit)
      }
    } else {
      client.cache.cacheGenerationOp(guid = msg.guid, operation = msg.operation, generationKey = msg.generationKey, parameters = None) onComplete {
        case Failure(t) => resultPromise.failure(t)
        case Success(g) => sendResponse(g.toArray) onComplete {
          case Failure(t) => resultPromise.failure(t)
          case Success(_) => resultPromise.success((): Unit)
        }
      }
    }
    Await.result(resultPromise.future, 10 minutes)
  }

  /**
   * Respond to a request from the supervisor requesting slices for a particular generation key
   */
  final
  def cacheSliceFetch(msg: FabricNetSliceFetchReqMsg): Unit = {
    val resultPromise = Promise[Unit]()

    def sendResponse(slices: Array[FabricSliceMetadata]): Future[Unit] = {
      val responsePromise = Promise[Unit]()
      transmitter transmitControlMessage FabricNetSliceFetchRespMsg(msg, clientKey, serverKey, slices) onComplete {
        case Failure(t) => responsePromise.failure(t)
        case Success(_) => responsePromise.success((): Unit)
      }
      responsePromise.future
    }

    if (client.cache == null) {
      sendResponse(Array.empty) onComplete {
        case Failure(t) => resultPromise.failure(t)
        case Success(_) => resultPromise.success((): Unit)
      }
    } else {
      client.cache.cacheSliceOp(guid = msg.guid, generationKey = msg.generationKey) onComplete {
        case Failure(t) => resultPromise.failure(t)
        case Success(g) => sendResponse(g.toArray) onComplete {
          case Failure(t) => resultPromise.failure(t)
          case Success(_) => resultPromise.success((): Unit)
        }
      }
    }
    Await.result(resultPromise.future, 10 minutes)
  }

}
