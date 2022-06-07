/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.background

import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsPojo
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.background.VitalsBackgroundFunctions.BackgroundFunction
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object VitalsBackgroundFunctions {

  /**
   * background function is executed serially with other background functions by a single background thread
   */
  type BackgroundFunction = () => Unit

}

/**
 * A class for simple periodic background threading shared among a dynamic list of functions.
 * Both start and stop are synchronous i.e. you are sure the background thread is started and stopping
 *
 * @param name      user friendly name for this
 * @param startWait how long before first function execution
 * @param period    how long between subsequent executions
 */
class VitalsBackgroundFunctions(name: String, startWait: Duration, period: Duration) extends VitalsService {

  override val modality: VitalsServiceModality = VitalsPojo

  override def serviceName: String = s"$name(startWait=$startWait, period=$period)"

  private[this] final val _runningLatch = new CountDownLatch(1)

  private[this] final val _stoppedLatch = new CountDownLatch(1)

  private[this] final val _stopping = new AtomicBoolean(false)

  private[this] final val _stopped = new AtomicBoolean(false)

  private[this] final val _functionList = ConcurrentHashMap.newKeySet[BackgroundFunction]

  /**
   * add a background function
   *
   * @param function
   */
  def +=(function: BackgroundFunction): this.type = {
    _functionList add function
    this
  }

  def -=(function: BackgroundFunction): this.type = {
    _functionList remove function
    this
  }

  override
  def start: this.type = {
    synchronized {
      if (isRunning) return this
      log info startingMessage
      Future {
        Thread.currentThread setName name
        if (_runningLatch.await(startWait.toNanos, TimeUnit.NANOSECONDS)) {
          _stoppedLatch.countDown()
        }
        while (!_stopping.get()) {
          val actualPeriod = period
          try {
            val start = System.nanoTime
            // do them one at time to prevent large thread counts for lots of users
            _functionList.stream.forEach(f =>
              Try(f()) match {
                case Failure(t) => log warn burstStdMsg(s"VITALS_BACKGROUND_FUNCTIONS_FAIL $t", t)
                case Success(_) => // no-op
              })
            actualPeriod.minus((System.nanoTime() - start) nanos)
            if (actualPeriod.toNanos <= 0)
              log warn burstStdMsg(s"VITALS_BACKGROUND_FUNCTIONS_TIMEOUT (actualTime=${actualPeriod.toNanos})")
          } catch safely {
            case t: Throwable =>
              log error burstStdMsg(t)
          }
          if (_runningLatch.await(actualPeriod.max(1 nano).toNanos, TimeUnit.NANOSECONDS)) {
            _stoppedLatch.countDown()
          }
        }
        _stopped set true
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
      _stopping set true
      _runningLatch.countDown()
      while (!_stopped.get)
        _stoppedLatch.await(25, TimeUnit.MILLISECONDS)
      log info stoppedMessage
      markNotRunning
    }
    this
  }

}
