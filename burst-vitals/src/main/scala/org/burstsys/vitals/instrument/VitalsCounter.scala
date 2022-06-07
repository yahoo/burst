/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.instrument

import java.text.DecimalFormat

final case
class VitalsCounter(item: String, window: Int, global: Boolean = false) {

  /**
    * This one is just to move the time window forward.
    * To be used when you want to record for bits of a complete transaction
    */
  def recordStart(): Unit = {
    _lastRecordedNanos = System.nanoTime()
  }

  /**
    * This is when you want to record an action processing x bytes
    *
    * @param bytes
    * @return
    */
  def recordBytes(bytes: Long, forceReport: Boolean = false): String = {
    _count += 1
    _bytes += bytes
    reportStats()
  }

  def forceReportStats(): String = {
    reportStats(true)
  }

  private[this] var _startNanos: Long = 0L
  private[this] var _count: Long = 0
  private[this] var _bytes: Long = 0
  private[this] var _expiredNanos: Long = 0L
  private[this] var _lastRecordedNanos: Long = 0L

  def open: this.type = {
    _startNanos = System.nanoTime
    _lastRecordedNanos = _startNanos
    this
  }

  private[this]
  def reportStats(forceReport: Boolean = false): String = {
    val _currentNanos = System.nanoTime()
    _expiredNanos += _currentNanos - _lastRecordedNanos
    _lastRecordedNanos = _currentNanos

    if (_count % window == 0 || forceReport) {
      val doubleFormat = new DecimalFormat("#.##")
      val msg = s"${item}s=${_count}, ${item}sPerSec=${
        doubleFormat.format(_count.toDouble / (_expiredNanos.toDouble / 1e9))
      }, ${item}Bytes=${_bytes}, ${item}BytesPerSec=${
        doubleFormat.format(_bytes.toDouble / (_expiredNanos.toDouble / 1e9))
      }"
      if (!global) {
        _count = 0
        _bytes = 0
        _startNanos = System.nanoTime
        _lastRecordedNanos = _startNanos
        _expiredNanos = 0
      }
      msg
    } else null
  }

  case class VitalsCounterInfo(count: Long, bytes: Long)

  def probeCounterInfo() = VitalsCounterInfo(_count, _bytes)

}
