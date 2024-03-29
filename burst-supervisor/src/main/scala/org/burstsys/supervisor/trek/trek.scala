/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor

import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekSupervisor}

package object trek {

  object RunQueryTrek extends VitalsTrekMark(
    "RunQuery", VitalsTrekCell, VitalsTrekSupervisor, SpanKind.SERVER, root = true
  )

  object StartBurnInTrek extends VitalsTrekMark(
    "StartBurnIn", VitalsTrekCell, VitalsTrekSupervisor, SpanKind.SERVER, root = true
  )

  object BurnInDatasetTrek extends VitalsTrekMark(
    "BurnInDataset", VitalsTrekCell, VitalsTrekSupervisor, root = true
  )

  object BurnInQueryTrek extends VitalsTrekMark(
    "BurnInQuery", VitalsTrekCell, VitalsTrekSupervisor, SpanKind.CLIENT
  )
}
