/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore

import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry
import org.burstsys.samplesource.handler.SimpleSampleStoreApiServerDelegate
import org.burstsys.samplesource.nexus.SampleSourceNexusServer
import org.burstsys.samplestore.api.server.SampleStoreApiServer
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.properties.VitalsPropertyMap

import java.util.concurrent.TimeUnit

case class SyntheticSampleStoreContainer(
                                          modality: VitalsServiceModality,
                                          var storeListenerProperties: VitalsPropertyMap = Map.empty
                                        ) extends VitalsService {

  private val thriftApiServer = SampleStoreApiServer(SimpleSampleStoreApiServerDelegate(storeListenerProperties))

  override def start: SyntheticSampleStoreContainer.this.type = {
    SampleSourceHandlerRegistry.start
    if (modality.isServer) {
      thriftApiServer.start
    } else {
      SampleSourceNexusServer.start
    }
    this
  }

  /**
   * Busy wait to keep a main routine alive
   */
  def run(): Unit = {
    while (true) {
      Thread.sleep(TimeUnit.SECONDS.toMillis(60))
    }
  }

  override def stop: SyntheticSampleStoreContainer.this.type = {
    if (modality.isServer) {
      thriftApiServer.stop
    } else {
      SampleSourceNexusServer.stop
    }
    this
  }
}
