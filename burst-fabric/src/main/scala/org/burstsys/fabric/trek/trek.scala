/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.vitals.trek._

package object trek {

  final object FabricMasterWaveTrekMark extends VitalsTrekMark("fabric_master_wave",
    cluster = VitalsTrekCell,
    role = VitalsTrekMaster
  )

  final object FabricMasterParticleTrekMark extends VitalsTrekMark("fabric_master_particle",
    cluster = VitalsTrekCell,
    role = VitalsTrekMaster
  )

  final object FabricMasterRequestTrekMark extends VitalsTrekMark("fabric_master_request",
    cluster = VitalsTrekCell,
    role = VitalsTrekMaster
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
