/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekClient, VitalsTrekMark, VitalsTrekServer}

package object trek {

  final object FabricNetHeartbeat extends VitalsTrekMark("FabricNetHeartbeat",
    VitalsTrekCell, VitalsTrekClient, SpanKind.CLIENT, root = true
  )

  final object FabricNetHeartbeatRcv extends VitalsTrekMark("FabricNetHeartbeatRcv",
    VitalsTrekCell, VitalsTrekServer, SpanKind.SERVER
  )

  final object FabricNetAssessResp extends VitalsTrekMark("FabricNetAssessResp",
    VitalsTrekCell, VitalsTrekClient, SpanKind.SERVER
  )

  final object FabricNetAssess extends VitalsTrekMark("FabricNetAssessReq",
    VitalsTrekCell, VitalsTrekServer, SpanKind.CLIENT, root = true
  )
}
