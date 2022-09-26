/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.handler

import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreApiServerDelegate
import org.burstsys.samplestore.api.SampleStoreGeneration
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.properties._
import org.burstsys.vitals.errors.safely

import scala.concurrent.Future

case class SimpleSampleStoreApiServerDelegate(listenerProperties: VitalsPropertyMap) extends SampleStoreApiServerDelegate {

  override def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[SampleStoreGeneration] = {
    val sourceName = dataSource.view.storeProperties.getValueOrThrow[String](SampleStoreSourceNameProperty)
    log info s"Handling view generation request guid=$guid sourceName=$sourceName"
    try {
      SampleSourceHandlerRegistry.getSupervisor(sourceName).getViewGenerator(guid, dataSource, listenerProperties)
    } catch safely {
      case e =>
        log error("View generation request failed guid=$guid", e)
        throw e
    }
  }

}
