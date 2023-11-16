/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.client

import com.twitter.util._
import org.burstsys.api.BurstApiClient
import org.burstsys.samplestore.api.{BurstSampleStoreApiService, BurstSampleStoreApiViewGenerator, BurstSampleStoreDataSource, SampleStoreApi}
import org.burstsys.samplestore.api.trek.SampleStoreViewGeneratorTrek
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsStandardClient
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.net.{VitalsHostName, VitalsHostPort}

import scala.language.postfixOps

/**
  * client side implementation of the sample store Thrift client (this would be on the burst cell side)
  */
final case
class SampleStoreApiClient(
                            override val apiHost: VitalsHostName = defaultHostName,
                            override val apiPort: VitalsHostPort = defaultPort
                          ) extends BurstApiClient[BurstSampleStoreApiService.MethodPerEndpoint] with SampleStoreApi {

  override def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[BurstSampleStoreApiViewGenerator] = {
    SampleStoreViewGeneratorTrek.begin(guid) { stage =>
      try {
        ensureRunning
        log info s"Sending view generation request guid=$guid targetHost=$apiHost"
        val r = thriftClient.getViewGenerator(guid, dataSource)
        SampleStoreViewGeneratorTrek.end(stage)
        r
      } catch safely {
        case e: Exception =>
          log error(s"View generation request failed guid=$guid datasource=$dataSource", e)
          SampleStoreViewGeneratorTrek.fail(stage, e)
          throw e
      }
    }
  }

  override def modality: VitalsService.VitalsServiceModality = VitalsStandardClient

  val created: Long = System.currentTimeMillis()
}
