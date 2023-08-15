package org.burstsys.vitals

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{SpanId, TraceId, Tracer}
import org.apache.commons.codec.digest.DigestUtils
import org.burstsys.vitals.logging.{VitalsLogger, burstLocMsg}
import org.burstsys.vitals.uid.VitalsUid

import java.nio.charset.StandardCharsets

package object tracing extends VitalsLogger {
  final def tracer: Tracer = GlobalOpenTelemetry.getTracer("org.burstsys")

  def uidToTraceId(trekId: VitalsUid): String = {
    val idHash = DigestUtils.sha256Hex(trekId.getBytes(StandardCharsets.UTF_8))
    idHash.take(TraceId.getLength)
  }

}
