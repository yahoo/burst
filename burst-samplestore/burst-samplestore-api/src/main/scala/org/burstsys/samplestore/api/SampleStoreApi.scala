/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import com.twitter.finagle.service
import org.burstsys.api.BurstApi
import org.burstsys.samplestore.api.configuration.SampleStoreApiProperties
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import scala.concurrent.duration.Duration

/**
  * A front end to the underlying thrift API
  */
trait SampleStoreApi extends VitalsService with BurstSampleStoreApiService.MethodPerEndpoint with SampleStoreApiProperties {

  def modality: VitalsServiceModality

  final override def apiName: String = "samplestore"

}
