/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekRemote, VitalsTrekWorker}

package object trek {

  final object NexusClientStreamTrekMark extends VitalsTrekMark("nexus_client_stream",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object NexusServerStreamTrekMark extends VitalsTrekMark("nexus_server_stream",
    cluster = VitalsTrekRemote,
    role = VitalsTrekWorker
  )

  final object NexusServerCompleteSendTrekMark extends VitalsTrekMark("nexus_server_complete_send",
    cluster = VitalsTrekRemote,
    role = VitalsTrekWorker
  )

  final object NexusServerParcelSendTrekMark extends VitalsTrekMark("nexus_server_parcel_send",
    cluster = VitalsTrekRemote,
    role = VitalsTrekWorker
  )


}
