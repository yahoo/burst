/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.server

import com.twitter
import com.twitter.util.Future
import org.burstsys.api.BurstApiServer
import org.burstsys.api.scalaToTwitterFuture
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestContext
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiNotReady
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestException
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestInvalid
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestTimeout
import org.burstsys.samplestore.api.BurstSampleStoreApiViewGenerator
import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreApi
import org.burstsys.samplestore.api.SampleStoreApiServerDelegate
import org.burstsys.samplestore.api.SampleStoreApiNotReadyException
import org.burstsys.samplestore.api.SampleStoreApiRequestInvalidException
import org.burstsys.samplestore.api.SampleStoreApiRequestTimeoutException
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.burstsys.vitals.errors.safely


/**
 * samplesource side implementation of the sample store Thrift server
 *
 * @param service
 */
final case
class SampleStoreApiServer(delegate: SampleStoreApiServerDelegate) extends BurstApiServer with SampleStoreApi {

  override def modality: VitalsService.VitalsServiceModality = VitalsStandardServer

  override
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): twitter.util.Future[BurstSampleStoreApiViewGenerator] = {
    ensureRunning
    try {
      scalaToTwitterFuture(delegate.getViewGenerator(guid, dataSource)) map { generation =>
        BurstSampleStoreApiViewGenerator(
          BurstSampleStoreApiRequestContext(guid), generation.generationHash, Some(generation.loci), generation.motifFilter
        )
      } handle {
        case t: Throwable =>
          val status = t match {
            case _: SampleStoreApiRequestTimeoutException => BurstSampleStoreApiRequestTimeout
            case _: SampleStoreApiRequestInvalidException => BurstSampleStoreApiRequestInvalid
            case _: SampleStoreApiNotReadyException => BurstSampleStoreApiNotReady
            case _ => BurstSampleStoreApiRequestException
          }
          BurstSampleStoreApiViewGenerator(BurstSampleStoreApiRequestContext(guid, status, t.toString), "NO_HASH")
      }
    } catch safely {
      case t: Throwable =>
        Future.value(
          BurstSampleStoreApiViewGenerator(BurstSampleStoreApiRequestContext(guid, BurstSampleStoreApiRequestException, t.toString), "NO_HASH")
        )
    }
  }

}
