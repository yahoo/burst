/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter.machine

import io.opentelemetry.api.trace.Span
import org.burstsys.tesla.scatter.{TeslaScatterContext, TeslaScatterFailState, TeslaScatterRunState, TeslaScatterStopState, TeslaScatterSuccessState}
import org.burstsys.vitals.errors.VitalsException

import scala.language.postfixOps

/**
 * Scatters are managed by a state machine / event queue mechanism. There is one event queue and
 * all state changes are posted onto that queue and the client/consumer of that queue uses
 * a single thread to process events.
 */
trait TeslaScatterMachine extends AnyRef
  with TeslaScatterLifecycle with TeslaScatterTerminator with TeslaScatterTender {

  self: TeslaScatterContext =>

  /**
   *
   */
  final override
  def execute(): Unit = {
    lazy val tag = s"TeslaScatterMachine.execute(guid=$guid, traceId=${Span.current.getSpanContext.getTraceId})"
    lockScatter("execute")
    try {
      _scatterState match {
        case TeslaScatterStopState =>
          _scatterState = TeslaScatterRunState
        case _ => throw VitalsException(s"$tag $this can't start from state ${_scatterState}")
      }
      log info s"SCATTER_EXECUTE $this activeSlots=${_activeSlots.size} $tag "

      _startNanos = System.nanoTime
      pushUpdate(_begin)
      // TODO the returned futures - should we keep track of these?
      _activeSlots.values foreach (_.request.execute)
    } finally unlockScatter("execute")
  }

  final override
  def scatterSucceed(): Unit = {
    lazy val tag = s"TeslaScatterMachine.scatterSucceed(guid=$guid)"
    lockScatter("succeed")
    try {
      log debug s"$tag $this"
      pushUpdate(_success)
      _scatterState = TeslaScatterSuccessState
    } finally unlockScatter("succeed")
  }

  final override
  def scatterCancel(message: String): Unit = {
    lazy val tag = s"TeslaScatterMachine.scatterCancel(guid=$guid)"
    lockScatter("cancel")
    try {
      log debug s"$tag $this"
      zombifySlots()

      _cancelled.message = message
      pushUpdate(_cancelled)

      _scatterState = TeslaScatterStopState
    } finally unlockScatter("cancel")
  }

  final override
  def scatterFail(throwable: Throwable): Unit = {
    lazy val tag = s"TeslaScatterMachine.scatterFail(guid=$guid)"
    lockScatter("fail")
    try {
      log debug s"$tag $this"
      zombifySlots()

      _fail.throwable = throwable
      pushUpdate(_fail)

      _scatterState = TeslaScatterFailState
    } finally unlockScatter("fail")
  }

  final override
  def scatterTimeout(message: String): Unit = {
    val tag = s"TeslaScatterMachine.scatterTimeout(guid=$guid)"
    lockScatter("timeout")
    try {
      log debug s"$tag $this"
      zombifySlots()

      _timeout.message = message
      pushUpdate(_timeout)

      _scatterState = TeslaScatterStopState
    } finally unlockScatter("timeout")
  }

}
