/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave

import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek._

package object trek {

  final object FabricSupervisorWaveTrekMark extends VitalsTrekMark("FabricSupervisorWave",
    VitalsTrekCell, VitalsTrekSupervisor
  )

  final object FabricSupervisorParticleTrekMark extends VitalsTrekMark("FabricSupervisorParticle",
    VitalsTrekCell, VitalsTrekSupervisor
  )

  final object FabricWorkerRequestTrekMark extends VitalsTrekMark("FabricWorkerRequest",
    VitalsTrekCell, VitalsTrekWorker, SpanKind.SERVER
  )

  final object FabricWorkerFetchTrekMark extends VitalsTrekMark("FabricWorkerFetch",
    VitalsTrekCell, VitalsTrekWorker, SpanKind.SERVER
  )

  final object FabricWorkerScanTrekMark extends VitalsTrekMark("FabricWorkerScan",
    VitalsTrekCell, VitalsTrekWorker
  )

  final object FabricWorkerScanInitTrekMark extends VitalsTrekMark("FabricWorkerScanInit",
    VitalsTrekCell, VitalsTrekWorker
  )

}
