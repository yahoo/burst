/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import java.util.concurrent.atomic.AtomicLong

/**
 * a sampling component in the reporter types system
 */
trait VitalsReporterSampler extends AnyRef {

  /**
   * the name for this datum
   */
  def dName: String

  /**
   * called periodically to give reporter the opportunity to record a sample (not all reporters use this)
   *
   * @param sampleMs
   */
  def sample(sampleMs: Long): Unit

  /**
   * the epoch time the last sample was taken
   *
   * @return
   */
  final def lastSampleEpoch: Long = _lastSampleEpoch.longValue

  /**
   * called by concrete subtypes to mark a new sample has been recorded
   * Call this ''before'' actually recording any data
   */
  final def newSample(): Unit = {
    // track time this sample was taken
    _lastSampleEpoch.set(System.currentTimeMillis)
  }

  /**
   * either there is no data yet or no new data since ``staleAfterPeriod``
   *
   * @return
   */
  final def nullData: Boolean = {
    if (_lastSampleEpoch.longValue == -1) true
    else {
      val elapsed = System.currentTimeMillis - _lastSampleEpoch.longValue
      val nullData = elapsed > staleAfterPeriod.toMillis
      nullData
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _lastSampleEpoch: AtomicLong = new AtomicLong(-1)
}
