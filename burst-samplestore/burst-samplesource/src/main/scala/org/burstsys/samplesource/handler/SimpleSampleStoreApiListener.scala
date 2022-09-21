/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.handler

import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreApiListener
import org.burstsys.samplestore.api.SampleStoreGeneration
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.properties._

import scala.concurrent.Future

case class SimpleSampleStoreApiListener(listenerProperties: VitalsPropertyMap) extends SampleStoreApiListener {

  override def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[SampleStoreGeneration] = {
    val sourceName = dataSource.view.storeProperties.getValueOrThrow[String](SampleStoreSourceNameProperty)
    val handler = SampleSourceHandlerRegistry.getMaster(sourceName)
    handler.getViewGenerator(guid, dataSource, listenerProperties)
  }

}
