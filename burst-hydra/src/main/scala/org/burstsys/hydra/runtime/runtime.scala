/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra

import java.util.concurrent.locks.ReentrantLock

import org.burstsys.hydra.sweep.HydraSweep
import org.burstsys.tesla.pool.{TeslaPoolId, TeslaResourcePool}
import org.burstsys.tesla.thread.worker.assertInTeslaWorkerThread
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.VitalsUnitQuantReporter

import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.language.postfixOps

package object runtime extends VitalsLogger {

  object HydraGatherReporter extends VitalsUnitQuantReporter("hydra", "gather")

  final val maxGenerateDuration: Duration = 10 minutes

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // DEBUGGING SUPPORT
  ///////////////////////////////////////////////////////////////////////////////////////////////

  final var StaticSweep: HydraSweep = _

  /**
   * when debugging sometimes its helpful to enforce single threaded behavior
   */
  final var SerializeTraversal: Boolean = false

  private[this] final val _lock = new ReentrantLock

  /**
   * if enabled wait for a single threaded moment
   * @return
   */
  @inline final
  def startSerialTraversal: this.type = {
    if (SerializeTraversal)
      _lock.lock()
    this
  }

  /**
   * if enabled, let the next thread have its single threaded moment
   * @return
   */
  @inline final
  def endSerialTraversal: this.type = {
    if (SerializeTraversal)
      _lock.unlock()
    this
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // GATHER POOLING
  ///////////////////////////////////////////////////////////////////////////////////////////////

  final
  val hydraGatherPoolId: TeslaPoolId = 27

  private
  lazy val _pool = TeslaResourcePool[HydraGather]("HYDRA_GATHER", hydraGatherPoolId, 1000000)

  @inline final
  def grabGather(scanner: HydraScanner): HydraGather = {
    // make sure there are limited set of threads doing allocations
    assertInTeslaWorkerThread()
    HydraGatherReporter.grab()
    _pool poolGrab match {
      case null =>
        HydraGather().initialize(scanner)
      case r =>
        r.initialize(scanner)
        r
    }
  }

  @inline final
  def releaseGather(d: HydraGather): Unit = {
    // make sure there are limited set of threads doing allocations
    assertInTeslaWorkerThread()
    HydraGatherReporter.release()
    _pool poolRelease d
  }

}
