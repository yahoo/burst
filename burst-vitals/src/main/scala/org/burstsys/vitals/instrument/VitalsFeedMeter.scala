/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.instrument

import java.util.concurrent.atomic.AtomicLong

/**
  * simple feed instrumentation
  * @deprecated
  */
final case class VitalsFeedMeter(`type`: String, logPeriod: Long) {
  private val operationCount: AtomicLong = new AtomicLong(0)
  private val sizeTally: AtomicLong = new AtomicLong(0)
  private val maxSize: AtomicLong = new AtomicLong(0)
  private var rateTimer: VitalsRateMeter = _
  private var operation: String = _

  this.operation = `type`
  rateTimer = VitalsRateMeter(`type`)

  def start: this.type = {
    rateTimer.start
    this
  }

  def stop: this.type = {
    rateTimer.stop
    this
  }

  def reading(operation: String, size: Long): String = {
    val message: String = rateTimer.periodRead(operationCount.incrementAndGet, logPeriod)
    var maxSize: Long = 0
    sizeTally.addAndGet(size)
    this.maxSize synchronized {
      maxSize = Math.max(this.maxSize.get, size)
      this.maxSize.set(maxSize)
    }
    val avgSize: Long = sizeTally.get / operationCount.get
    if (message != null) {
      val maxKB: Double = maxSize / 1.0
      val avgKB: Double = avgSize / 1.0
      "%s -> %s (size max/avg: %s/%s)".format(operation, message, prettyByteSizeString(maxKB.toLong),
        prettyByteSizeString(avgKB.toLong))
    } else
      null
  }

}
