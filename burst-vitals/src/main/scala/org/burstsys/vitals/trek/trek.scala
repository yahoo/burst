/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.trace._
import io.opentelemetry.context.{Context, Scope}
import io.opentelemetry.sdk.trace.IdGenerator
import org.apache.commons.codec.digest.DigestUtils
import org.burstsys.vitals
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

import java.nio.charset.StandardCharsets
import scala.annotation.unused

package object trek extends VitalsLogger {
  lazy private[vitals]
  val _tracer = tracing.tracer

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

  object VitalsTrekServer extends VitalsTrekRole("supervisor")

  object VitalsTrekClient extends VitalsTrekRole("client")

  // these names must conform to open telemetry attribute naming standards.
  private val NAME_KEY = "trek.name"
  private val TREK_ID = "trek.id"
  private val CALL_ID = "trek.callId"
  private val CLUSTER_KEY = "cluster"

  case class TrekStage(span: Span, scope: Scope) {

    def addEvent(name: String): this.type = {
      span.addEvent(name)
      this
    }

    def setStatus(status: StatusCode): this.type = {
      span.setStatus(status)
      this
    }

    def end(): Unit = {
      scope.close()
      span.end()
    }

    def closeScope(): Unit = scope.close()
  }

  case class VitalsTrekMark(
                             name: String,
                             @unused cluster: VitalsTrekCluster,
                             @unused role: VitalsTrekRole,
                             kind: SpanKind = SpanKind.INTERNAL,
                             root: Boolean = false
                           ) {

    /**
     * Wrap a block of code in a trek stage. The trek's scope will be closed when the block exits.
     *
     * @param trekId the id of the trek
     * @param callId the id of the call
     * @param wrapped the block of code to wrap
     * @tparam R the return type of the block
     * @return the return value of the block
     */
    def begin[R](trekId: VitalsUid = null, callId: VitalsUid = null)(wrapped: TrekStage => R): R = {
      val stage: TrekStage = beginSync(trekId, callId)
      try {
        wrapped(stage)
      } finally {
        stage.closeScope()
      }
    }

    /**
     * Start a trek and return a TrekStage. The caller is responsible for closing the scope before
     * relinquishing the executing thread.
     * @param trekId the id of the trek
     * @param callId the id of the call
     * @return the TrekStage
     */
    def beginSync(trekId: VitalsUid = null, callId: VitalsUid = null): TrekStage = {
      // build an acceptable OT trace id from the unstructured Trek id
      if (log.isDebugEnabled)
        log debug burstLocMsg(s"start trek $name trekId=$trekId callId=$callId parent=${Span.current}")

      val parent = if (root) Context.root else Context.current
      val context = if (Baggage.current.getEntryValue(TREK_ID) != trekId) {
        Baggage.current.toBuilder
          .put(TREK_ID, trekId)
          .put(CALL_ID, callId).build()
          .storeInContext(parent)
      } else parent

      val builder = vitals.tracing.tracer.spanBuilder(name)
        .setSpanKind(kind)
        .setParent(context)
        .setAttribute(NAME_KEY, name)

      if (root) {
        builder.addLink(Span.current.getSpanContext)
      }

      Baggage.current.forEach((key, entry) => {
        log debug burstLocMsg(s"trek $name baggage $key=${entry.getValue}")
        builder.setAttribute(key, entry.getValue)
      })

      val span = builder.startSpan()

      val stage = TrekStage(span, span.makeCurrent())
      span.addEvent("BEGIN") // the begin event is important to the TrekSpanProcessor and TrekLoggingSpanExporter
      if (log.isDebugEnabled)
        log debug burstLocMsg(s"start trek $name returns span=$span")
      stage
    }

    final def end(stage: TrekStage): Unit = {
      if (log.isDebugEnabled) {
        log debug burstLocMsg(s"end trek $name span=$stage")
      }
      stage.end()
    }

    final def fail(stage: TrekStage, exception: Throwable=null): Unit = {
      if (log.isDebugEnabled) {
        log debug burstLocMsg(s"fail trek $name span=$stage exception=$exception")
      }
      if (exception != null) {
        stage.span.recordException(exception)
      }
      stage
        .setStatus(StatusCode.ERROR)
        .end()
    }
  }
}
