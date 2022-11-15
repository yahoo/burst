/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore.worker

import org.burstsys.json.samplestore.configuration.jsonLoadConcurrencyProperty
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.uid.VitalsUid

import java.util.concurrent.{Semaphore, TimeUnit, TimeoutException}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * This throttle counts waves passing through its gate, and limits the number, blocking new ones and waiting
 * for a prior operation to finish before letting the new ones through.
 */
object JsonLoadThrottle {

  private val MaxPressScheduleWait = 1 minute
  private val MaxConcurrentLoads = jsonLoadConcurrencyProperty.get
  private[this] val _loadGate = new Semaphore(MaxConcurrentLoads, true)

  private def freeResourcesAvailableForLoad: Boolean = false

  private def concurrentLoadCount: Int = MaxConcurrentLoads - _loadGate.availablePermits()

  final def apply[ResultType <: Any](guid: VitalsUid, doLoad: => ResultType): ResultType = {
    val tag = s"AlloyLoadThrottle guid=$guid"
    val start = System.nanoTime
    var permitted = false
    try {
      // TODO: investigate use of AbstractQueuedSynchronizer to ensure we gate resources usage in a FIFO maner
      // while (!freeResourcesAvailableForLoad) {
      //   Thread.sleep(50)
      //   if (System.nanoTime - start > MaxPressScheduleWait.toNanos) {
      //     throw new TimeoutException("Permitted but no resources available")
      //   }
      // }
      log info s"$tag Checking resource availability"
      permitted = _loadGate.tryAcquire(MaxPressScheduleWait.toNanos, TimeUnit.NANOSECONDS)
      if (!permitted)
        throw new TimeoutException(s"Too many loads already running. loadsRunning=${concurrentLoadCount}")

      JsonSampleSourceWorkerReporter.recordConcurrency(concurrentLoadCount)
      log info s"$tag delay=${System.nanoTime - start} Beginning load after ${prettyTimeFromNanos(System.nanoTime - start)}"
      doLoad
    } finally {
      if (permitted) {
        _loadGate.release()
      }
      log info s"$tag totalDuration=${System.nanoTime - start} permitted=$permitted Exit load throttle"
    }
  }
}
