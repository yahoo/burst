/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore

import org.burstsys.samplesource.SampleSourceId
import org.burstsys.samplesource.handler.SampleSourceHandler.sampleSourceHandler
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, SampleStoreApiListener, SampleStoreGenerator, SampleStoreSourceNameProperty}
import org.burstsys.vitals.properties._

import scala.concurrent.Future

/**
 */
final case
class JsonSampleStoreListener(listenerProperties: VitalsPropertyMap) extends SampleStoreApiListener {

  override
  def getViewGenerator(guid: scala.Predef.String,
                       dataSource: BurstSampleStoreDataSource): Future[SampleStoreGenerator] = {
    val sourceName: SampleSourceId =
      dataSource.view.storeProperties.getValueOrThrow[SampleSourceId](SampleStoreSourceNameProperty)
    val handler = sampleSourceHandler(sourceName)
    handler.getViewGenerator(guid: scala.Predef.String, dataSource, listenerProperties)
  }

}
