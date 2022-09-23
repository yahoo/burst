/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.client

import com.twitter.util._
import org.burstsys.api.BurstApiClient
import org.burstsys.samplestore.api.BurstSampleStoreApiService
import org.burstsys.samplestore.api.BurstSampleStoreApiViewGenerator
import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreApi
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsStandardClient

import scala.language.postfixOps

/**
  * client side implementation of the sample store Thrift client (this would be on the burst cell side)
  */
final case
class SampleStoreApiClient() extends BurstApiClient[BurstSampleStoreApiService.MethodPerEndpoint] with SampleStoreApi {

  override def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[BurstSampleStoreApiViewGenerator] = {
    ensureRunning
    log info s"Sending view generation request guid=$guid targetHost=$apiHost"
    thriftClient.getViewGenerator(guid, dataSource)
  }

  override def modality: VitalsService.VitalsServiceModality = VitalsStandardClient
}
