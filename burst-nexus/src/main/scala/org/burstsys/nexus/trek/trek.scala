/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekRemote, VitalsTrekWorker}

package object trek {

  final object NexusTransmitTrekMark extends VitalsTrekMark("NexusTransmit",
    VitalsTrekCell, VitalsTrekWorker
  )

  final object NexusReceiveTrekMark extends VitalsTrekMark("NexusReceive",
    VitalsTrekCell, VitalsTrekWorker
  )

  final object NexusClientStreamStartTrekMark extends VitalsTrekMark("NexusClientStreamStart",
    VitalsTrekCell, VitalsTrekWorker, SpanKind.CLIENT
  )

  final object NexusClientStreamFinalizeTrekMark extends VitalsTrekMark("NexusClientStreamFinalize",
    VitalsTrekCell, VitalsTrekWorker
  )

  final object NexusServerStreamTrekMark extends VitalsTrekMark("NexusServerStream",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.SERVER
  )

  final object NexusServerCompleteSendTrekMark extends VitalsTrekMark("NexusServerCompleteSend",
    VitalsTrekRemote, VitalsTrekWorker
  )

  final object NexusServerParcelSendTrekMark extends VitalsTrekMark("NexusServerParcelSend",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.PRODUCER
  )


}
