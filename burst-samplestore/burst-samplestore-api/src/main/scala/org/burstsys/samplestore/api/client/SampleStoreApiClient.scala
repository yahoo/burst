/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.client

import org.burstsys.api.BurstApiClient
import org.burstsys.samplestore.api.{BurstSampleStoreApiService, BurstSampleStoreApiViewGenerator, BurstSampleStoreDataSource, SampleStoreApi, SampleStoreApiService}
import com.twitter.util._

import scala.language.postfixOps

/**
  * client side implementation of the sample store Thrift client (this would be on the burst cell side)
  */
private[samplestore] final case
class SampleStoreApiClient(service: SampleStoreApiService)
  extends BurstApiClient[BurstSampleStoreApiService.FutureIface] with SampleStoreApi {

  override
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[BurstSampleStoreApiViewGenerator] = {
    ensureRunning
    thriftClient.getViewGenerator(guid, dataSource)
  }

}