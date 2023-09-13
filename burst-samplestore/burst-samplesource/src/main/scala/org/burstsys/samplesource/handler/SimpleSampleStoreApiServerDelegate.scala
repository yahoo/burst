/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.handler

import org.burstsys.samplesource.SampleStoreTopologyProvider
import org.burstsys.samplesource.trek.SampleStoreViewGeneratorTrek
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, SampleStoreApiServerDelegate, SampleStoreGeneration, SampleStoreSourceNameProperty}
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.properties.{VitalsPropertyMap, _}

import scala.concurrent.Future

case class SimpleSampleStoreApiServerDelegate(topoProvider: SampleStoreTopologyProvider, listenerProperties: VitalsPropertyMap) extends SampleStoreApiServerDelegate {

  override def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[SampleStoreGeneration] = {
    val sourceName = dataSource.view.storeProperties.getValueOrThrow[String](SampleStoreSourceNameProperty)
    log info s"Handling view generation request guid=$guid sourceName=$sourceName"
    SampleStoreViewGeneratorTrek.begin(guid) { stage =>
      try {
        val supervisor = SampleSourceHandlerRegistry.getSupervisor(sourceName)
        val f = supervisor.getViewGenerator(guid, dataSource, topoProvider.getTopology, listenerProperties)
        SampleStoreViewGeneratorTrek.end(stage)
        f
      } catch safely {
        case e =>
          log error(s"View generation request failed guid=$guid datasource=$dataSource", e)
          SampleStoreViewGeneratorTrek.fail(stage, e)
          throw e
      }
    }
  }

}
