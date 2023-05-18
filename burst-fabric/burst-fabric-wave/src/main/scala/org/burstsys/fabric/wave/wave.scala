/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave

import org.burstsys.fabric.wave.data.model.slice.region.FabricRegionReporter
import org.burstsys.fabric.wave.data.worker.cache.FabricCacheReporter
import org.burstsys.fabric.wave.execution.supervisor.wave.FabricWaveReporter
import org.burstsys.fabric.wave.execution.worker.FabricEngineReporter
import org.burstsys.fabric.wave.metadata.model.FabricMetadataReporter
import org.burstsys.vitals.reporter.{VitalsReporter, VitalsReporterSource}

package object wave extends VitalsReporterSource {

  override def reporters: Array[VitalsReporter] = Array(
    FabricRegionReporter,
    FabricCacheReporter,
    FabricMetadataReporter,
    FabricWaveReporter,
    FabricEngineReporter
  )

}
