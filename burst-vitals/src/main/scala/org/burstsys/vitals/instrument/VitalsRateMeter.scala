/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.instrument

/**
  * simple rate instrumentation
  * @deprecated
  */
final case class VitalsRateMeter(name: String) {
  private[this] var startTime: Long = 0L

  def start: this.type = {
    startTime = System.nanoTime
    this
  }

  def stop: this.type = {
    this
  }

  def reset: VitalsRateMeter = {
    startTime = System.nanoTime
    this
  }

  def periodRead(count: Long, period: Long): String = {
    if (count % period == 0 && count != 0) {
      return String.format("%s", read(count))
    }
    null
  }

  def read(count: Long): String = {
    val now: Long = System.nanoTime
    val elapsed: Double = now - startTime
    val rate: Double = (count / elapsed) * 1e9
    "%,d %s(s) in %s (%,.2f per second)".format(count, name, prettyTimeFromNanos(elapsed), rate)
  }


}

