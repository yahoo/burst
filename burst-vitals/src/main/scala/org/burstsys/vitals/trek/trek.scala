/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace._
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.IdGenerator
import org.apache.commons.codec.digest.DigestUtils
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

import java.nio.charset.StandardCharsets

package object trek extends VitalsLogger {
  lazy private[vitals]
  val _tracer = GlobalOpenTelemetry.getTracer("org.burstsys")

  lazy private val protoSpan: Span = {
    if (Span.current().getSpanContext.isValid)
      Span.current()
    else
      _tracer.spanBuilder("proto").startSpan()
  }


  case class VitalsTrekCluster(name: String) {
    override def toString: VitalsUid = name
  }
  object VitalsTrekCell extends VitalsTrekCluster("cell")
  object VitalsTrekRemote extends VitalsTrekCluster("remote")

  case class VitalsTrekRole(name: String) {
    override def toString: VitalsUid = name
  }
  object VitalsTrekSupervisor extends VitalsTrekRole("supervisor")
  object VitalsTrekWorker extends VitalsTrekRole("worker")

  // these names must conform to open telemetry attribute naming standards.
  private val NAME_KEY = "trekname"
  private val ID_KEY = "trekid"
  private val SUB_ID_KEY = "subid"
  private val CLUSTER_KEY = "cluster"
  private val ROLE_KEY = "role"

  case
  class VitalsTrekMark(
                        name: String,
                        cluster: VitalsTrekCluster,
                        role: VitalsTrekRole
                      ) {
    lazy private[vitals]
    val _tracer = GlobalOpenTelemetry.getTracer("org.burstsys")

    private val trekSpanBuilder: SpanBuilder = _tracer.spanBuilder(name)
      .setAttribute(NAME_KEY, name)
      .setAttribute(ROLE_KEY, role.name)

    final
    def begin(trekId: VitalsUid, callId: VitalsUid = null): Span = {
      // build an acceptable OT trace id from the unstructured Trek id
      createSpanParent(trekId, callId).storeInContext(Context.current()).makeCurrent()
      val spanBuilder = trekSpanBuilder
      val span = spanBuilder
        .setAttribute(CLUSTER_KEY, cluster.name)
        .setAttribute(ID_KEY, trekId)
        .setAttribute(SUB_ID_KEY, callId)
        .startSpan()
      span.makeCurrent()
      span.addEvent("BEGIN")
      span
    }

    final
    def end(span: Span): Unit = {
      // assert(!Span.current().isRecording || span.asInstanceOf[ReadableSpan].toSpanData.getAttributes.get(AttributeKey.stringKey(NAME_KEY)) == name)
      span.end()
    }

    final
    def fail(span: Span): Unit = {
      // assert(!Span.current().isRecording || span.asInstanceOf[ReadableSpan].toSpanData.getAttributes.get(AttributeKey.stringKey(NAME_KEY)) == name)
      span.setStatus(StatusCode.ERROR).end()
    }

    /**
     * This uses open telemetry's context propagation to set the trace id to a value derived from the trek id allowing
     * us to take a trek id and associate it with the same trace no matter where we are.
     * This might not be needed if we implemented context propogation in the fabric and nexus layers.
     */
    protected
    def createSpanParent(trekId: VitalsUid, callId: VitalsUid): Span = {
      val traceId = trekToSpanId(trekId)
      if (Span.current().getSpanContext.getTraceId == traceId)
        return Span.current()

      // our current span traceid doesn't match the traceId/trekId so build the span at the root
      val spanId: String = {
        // we might need to recreate the span id from an external id
        if (callId == null) {
          IdGenerator.random().generateSpanId()
        } else {
          DigestUtils.sha256Hex(callId.getBytes(StandardCharsets.UTF_8))
        }
      }
      val traceStateBuilder = Span.current().getSpanContext.getTraceState.toBuilder
        .put(CLUSTER_KEY, cluster.name)
        .put(ID_KEY, trekId)
        .put(SUB_ID_KEY, callId)
      val context = SpanContext.create(
        traceId,
        spanId,
        protoSpan.getSpanContext.getTraceFlags,
        traceStateBuilder.build())
      Span.wrap(context)
    }

    protected
    def trekToSpanId(trekId: VitalsUid): String = {
      DigestUtils.sha256Hex(trekId.getBytes(StandardCharsets.UTF_8)).take(TraceId.getLength)
    }
  }
}
