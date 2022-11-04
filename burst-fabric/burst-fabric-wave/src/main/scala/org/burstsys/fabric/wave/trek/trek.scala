/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave

import org.burstsys.vitals.trek._

package object trek {

  final object FabricSupervisorWaveTrekMark extends VitalsTrekMark("fabric_supervisor_wave",
    cluster = VitalsTrekCell,
    role = VitalsTrekSupervisor
  )

  final object FabricSupervisorParticleTrekMark extends VitalsTrekMark("fabric_supervisor_particle",
    cluster = VitalsTrekCell,
    role = VitalsTrekSupervisor
  )

  final object FabricSupervisorRequestTrekMark extends VitalsTrekMark("fabric_supervisor_request",
    cluster = VitalsTrekCell,
    role = VitalsTrekSupervisor
  )

  final object FabricWorkerRequestTrekMark extends VitalsTrekMark("fabric_worker_request",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object FabricWorkerFetchTrekMark extends VitalsTrekMark("fabric_worker_fetch",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object FabricWorkerScanTrekMark extends VitalsTrekMark("fabric_worker_scan",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )


}
