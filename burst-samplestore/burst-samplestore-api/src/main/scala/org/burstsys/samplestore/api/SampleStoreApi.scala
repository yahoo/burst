/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import org.burstsys.samplestore.api.configuration.SampleStoreApiProperties
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality

trait SampleStoreAPIListener {

  def onViewGenerationRequest(guid: String, dataSource: BurstSampleStoreDataSource): Unit

  def onViewGeneration(guid: String, dataSource: BurstSampleStoreDataSource, result: BurstSampleStoreApiViewGenerator): Unit
}

/**
  * A front end to the underlying thrift API
  */
trait SampleStoreApi extends VitalsService with BurstSampleStoreApiService.MethodPerEndpoint with SampleStoreApiProperties {

  def modality: VitalsServiceModality

  final override def apiName: String = "samplestore"

}
