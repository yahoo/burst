/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import org.burstsys.api.BurstApi
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import scala.concurrent.duration.Duration

/**
  * A front end to the underlying thrift API
  */
trait SampleStoreApi extends VitalsService with BurstSampleStoreApiService.FutureIface with BurstApi {

  def service: SampleStoreApiService

  final override def modality: VitalsService.VitalsServiceModality = service.modality

  final override def apiName: String = "samplestore"

  final override def apiPort: VitalsHostPort = service.apiPort

  final override def maxConnectionIdleTime: Duration = service.maxConnectionIdleTime

  final override def maxConnectionLifeTime: Duration = service.maxConnectionLifeTime

  final override def apiHost: VitalsHostAddress = service.apiHost

  final override def enableSsl: Boolean = service.enableSsl

  final override def certPath: String = service.certPath

  final override def keyPath: String = service.keyPath

  final override def caPath: String = service.caPath

  final override def enableCompositeTrust: Boolean = service.enableCompositeTrust
}
