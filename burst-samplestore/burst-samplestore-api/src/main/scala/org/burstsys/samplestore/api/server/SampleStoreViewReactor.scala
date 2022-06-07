/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.server

import org.burstsys.api._
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.{BurstSampleStoreApiNotReady, BurstSampleStoreApiRequestException, BurstSampleStoreApiRequestInvalid, BurstSampleStoreApiRequestTimeout}
import org.burstsys.samplestore.api._
import org.burstsys.tesla.thread.request._
import com.twitter.util.Future

import scala.concurrent.Promise
import scala.util.{Failure, Success}

trait SampleStoreViewReactor extends SampleStoreApi {

  self: SampleStoreApiServer =>

  //////////////////////////////////////////////////////////////////////////////////////
  // private state
  //////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _listener: SampleStoreApiListener = _

  //////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////

  final
  def listener: SampleStoreApiListener = _listener

  final
  def talksTo(l: SampleStoreApiListener): this.type = {
    _listener = l
    this
  }

  final override
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[BurstSampleStoreApiViewGenerator] = {
    ensureRunning
    val promise = Promise[BurstSampleStoreApiViewGenerator]
    _listener.getViewGenerator(guid, dataSource) onComplete {
      case Failure(t) =>
        val result = t match {
          case timeout: SampleStoreApiRequestTimeoutException =>
            BurstSampleStoreApiViewGenerator(
              BurstSampleStoreApiRequestContext(guid, BurstSampleStoreApiRequestTimeout, t.toString),
              "NO_HASH"
            )
          case invalid: SampleStoreApiRequestInvalidException =>
            BurstSampleStoreApiViewGenerator(
              BurstSampleStoreApiRequestContext(guid, BurstSampleStoreApiRequestInvalid, t.toString),
              "NO_HASH"
            )
          case notReady: SampleStoreApiNotReadyException =>
            BurstSampleStoreApiViewGenerator(
              BurstSampleStoreApiRequestContext(guid, BurstSampleStoreApiNotReady, t.toString),
              "NO_HASH"
            )
          case _ =>
            BurstSampleStoreApiViewGenerator(
              BurstSampleStoreApiRequestContext(guid, BurstSampleStoreApiRequestException, t.toString),
              "NO_HASH"
            )
        }
        promise.success(result)

      case Success(operation) =>
        promise.success(
          BurstSampleStoreApiViewGenerator(
            BurstSampleStoreApiRequestContext(guid),
            operation.generationHash,
            Some(operation.loci),
            operation.motifFilter
          )
        )
    }
    promise.future
  }

}

