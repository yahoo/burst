/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.background

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * A class for simple periodic background threading. Both start and stop are synchronous i.e.
 * you are sure the background thread is started and stopping
 *
 * @param name      user friendly name for this
 * @param startWait how long before first function execution
 * @param period    how long between subsequent executions
 * @param function  function to execute
 */
class VitalsBackgroundFunction(name: String, startWait: Duration, period: Duration, function: => Unit) extends VitalsService {

  override val modality: VitalsServiceModality = VitalsPojo

  override def serviceName: String = s"$name(startWait=$startWait, period=$period)"

  private var continueExecutionLatch = new CountDownLatch(1)
  private var didStopLatch = new CountDownLatch(1)

  override
  def start: this.type = {
    synchronized {
      if (isRunning) return this

      continueExecutionLatch = new CountDownLatch(1)
      didStopLatch = new CountDownLatch(1)

      log info startingMessage

      Future {
        Thread.currentThread setName name
        if (continueExecutionLatch.await(startWait.toNanos, TimeUnit.NANOSECONDS)) {
          didStopLatch.countDown()
        }
        while (continueExecutionLatch.getCount > 0) {
          val actualPeriod = period
          try {
            val start = System.nanoTime
            function
            actualPeriod.minus((System.nanoTime() - start) nanos)
            if (actualPeriod.toNanos <= 0)
              log warn burstStdMsg(s"background thread ran out of time (actualTime=${actualPeriod.toNanos})")
          } catch safely {
            case t: Throwable =>
              log error burstStdMsg(t)
          }
          continueExecutionLatch.await(actualPeriod.max(1 nano).toNanos, TimeUnit.NANOSECONDS)
        }
        didStopLatch.countDown()
      }
      markRunning
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      if (!isRunning) return this
      log info stoppingMessage
      continueExecutionLatch.countDown()
      while (!didStopLatch.await(25, TimeUnit.MILLISECONDS)) {
        // empty loop to keep awaiting the stop latch
      }
      log info stoppedMessage
      markNotRunning
    }
    this
  }

}
