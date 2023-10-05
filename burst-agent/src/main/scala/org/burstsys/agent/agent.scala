/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek._
import org.burstsys.vitals.logging._

package object agent extends VitalsLogger {

  object AgentRequestTrekMark extends VitalsTrekMark(
    "AgentRequest", VitalsTrekCell, VitalsTrekSupervisor, SpanKind.SERVER)

}
