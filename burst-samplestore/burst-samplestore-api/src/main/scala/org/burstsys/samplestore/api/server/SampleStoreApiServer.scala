/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.server

import com.twitter.util.{Future => TFuture}
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
import org.burstsys.samplestore.api.SampleStoreApiListener
import org.burstsys.samplestore.api.SampleStoreApiNotReadyException
import org.burstsys.samplestore.api.SampleStoreApiRequestInvalidException
import org.burstsys.samplestore.api.SampleStoreApiRequestTimeoutException
import org.burstsys.samplestore.api.SampleStoreApiServerDelegate
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.safely

import java.util.concurrent.ConcurrentHashMap


/**
 * samplesource side implementation of the sample store Thrift server
 */
final case
class SampleStoreApiServer(delegate: SampleStoreApiServerDelegate) extends BurstApiServer with SampleStoreApi {

  private val _listeners = ConcurrentHashMap.newKeySet[SampleStoreApiListener]()

  override def modality: VitalsService.VitalsServiceModality = VitalsStandardServer

  def talksTo(listeners: SampleStoreApiListener*): this.type = {
    listeners.foreach(_listeners.add)
    this
  }

  private def notifyListeners(work: SampleStoreApiListener => Unit)
                             (implicit site: sourcecode.Enclosing, pkg: sourcecode.Pkg, file: sourcecode.FileName, line: sourcecode.Line): Unit = {
    _listeners.forEach(l => {
      try {
        work(l)
      } catch safely {
        case t =>
          log warn(s"Failed to notify listener ${l.getClass.getSimpleName}", VitalsException(t))
      }
    })

  }

  override
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): TFuture[BurstSampleStoreApiViewGenerator] = {
    try {
      ensureRunning
      notifyListeners(_.onViewGenerationRequest(guid, dataSource))
      scalaToTwitterFuture(delegate.getViewGenerator(guid, dataSource)) map { generation =>
        val result = BurstSampleStoreApiViewGenerator(
          BurstSampleStoreApiRequestContext(guid), generation.generationHash, Some(generation.loci), generation.motifFilter
        )
        notifyListeners(_.onViewGeneration(guid, dataSource, result))
        result
      } handle {
        case t: Throwable =>
          val status = t match {
            case _: SampleStoreApiRequestTimeoutException => BurstSampleStoreApiRequestTimeout
            case _: SampleStoreApiRequestInvalidException => BurstSampleStoreApiRequestInvalid
            case _: SampleStoreApiNotReadyException => BurstSampleStoreApiNotReady
            case _ => BurstSampleStoreApiRequestException
          }
          val result = BurstSampleStoreApiViewGenerator(BurstSampleStoreApiRequestContext(guid, status, t.toString), "NO_HASH")
          notifyListeners(_.onViewGeneration(guid, dataSource, result))
          result
      }
    } catch safely {
      case t: Throwable =>
        val result = BurstSampleStoreApiViewGenerator(BurstSampleStoreApiRequestContext(guid, BurstSampleStoreApiRequestException, t.toString), "NO_HASH")
        notifyListeners(_.onViewGeneration(guid, dataSource, result))
        TFuture.value(result)
    }
  }

}
