/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.server

import com.twitter.util.Future
import org.burstsys.api.BurstApiServer
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestContext
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiNotReady
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestException
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestInvalid
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestTimeout
import org.burstsys.samplestore.api.BurstSampleStoreApiViewGenerator
import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreApiListener
import org.burstsys.samplestore.api.SampleStoreApiNotReadyException
import org.burstsys.samplestore.api.SampleStoreApiRequestInvalidException
import org.burstsys.samplestore.api.SampleStoreApiRequestTimeoutException
import org.burstsys.samplestore.api.SampleStoreApi
import org.burstsys.samplestore.api.SampleStoreApiService
import org.burstsys.tesla.thread.request.FutureSemanticsEnhancer
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.api._

import scala.concurrent.Promise

/**
 * samplesource side implementation of the sample store Thrift server
 *
 * @param service
 */
private[samplestore] final case
class SampleStoreApiServer(service: SampleStoreApiService) extends BurstApiServer with SampleStoreApi {

  private[this]
  var _listener: SampleStoreApiListener = _

  //////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////

  def listener: SampleStoreApiListener = _listener

  def talksTo(l: SampleStoreApiListener): this.type = {
    _listener = l
    this
  }

  override
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[BurstSampleStoreApiViewGenerator] = {
    ensureRunning
    _listener.getViewGenerator(guid, dataSource) map { generator =>
      BurstSampleStoreApiViewGenerator(
        BurstSampleStoreApiRequestContext(guid), generator.generationHash, Some(generator.loci), generator.motifFilter
      )
    } recover {
      case t =>
        val status = t match {
          case _: SampleStoreApiRequestTimeoutException => BurstSampleStoreApiRequestTimeout
          case _: SampleStoreApiRequestInvalidException => BurstSampleStoreApiRequestInvalid
          case _: SampleStoreApiNotReadyException => BurstSampleStoreApiNotReady
          case _ => BurstSampleStoreApiRequestException
        }
        BurstSampleStoreApiViewGenerator(BurstSampleStoreApiRequestContext(guid, status, t.toString), "NO_HASH")
    }
  }
}
