/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.fabric.data.model.slice.region.FabricRegionReporter
import org.burstsys.fabric.data.worker.cache.FabricCacheReporter
import org.burstsys.fabric.execution.master.wave.FabricWaveReporter
import org.burstsys.fabric.execution.worker.FabricEngineReporter
import org.burstsys.fabric.metadata.model.FabricMetadataReporter
import org.burstsys.fabric.net.FabricNetReporter
import org.burstsys.fabric.topology.master.FabricTopologyReporter
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.VitalsReporterSource

package object fabric extends VitalsReporterSource {

  override def reporters: Array[VitalsReporter] = Array(
    FabricRegionReporter,
    FabricCacheReporter,
    FabricMetadataReporter,
    FabricWaveReporter,
    FabricNetReporter,
    FabricTopologyReporter,
    FabricEngineReporter
  )

}
