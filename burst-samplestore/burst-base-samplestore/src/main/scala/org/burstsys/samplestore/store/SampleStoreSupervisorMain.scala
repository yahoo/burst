/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store

import org.burstsys.fabric.net.server.defaultFabricNetworkServerConfig
import org.burstsys.samplestore.store.container.supervisor.SampleStoreFabricSupervisorContainerContext
import org.burstsys.vitals.logging.VitalsLog

object SampleStoreSupervisorMain {
  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("synthetic-supervisor")
    val properties = Map.empty[String, String]
    val supervisor = new SampleStoreFabricSupervisorContainerContext(defaultFabricNetworkServerConfig, properties)
    supervisor.start.run
  }
}
