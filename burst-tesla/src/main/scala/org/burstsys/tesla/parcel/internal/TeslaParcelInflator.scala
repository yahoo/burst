/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.internal

import org.burstsys.tesla.TeslaTypes.TeslaMemoryPtr
import org.burstsys.tesla.parcel.state.TeslaParcelState
import org.burstsys.tesla.parcel.{TeslaParcel, log}
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.logging.burstStdMsg
import org.xerial.snappy.Snappy

/**
 * decompression
 */
trait TeslaParcelInflator extends Any with TeslaParcel with TeslaParcelState {

  @inline final override
  def inflateTo(destination: TeslaMemoryPtr): Long = {
    lazy val tag = s"TeslaParcelInflator.inflateTo(blockPtr=$blockPtr, destination=$destination)"
    if (isInflated)
      throw VitalsException(s"ALREADY_INFLATED $this $tag")
    try {
      /*
        log info s"inflateTo() deflatedSize=$deflatedSize $this" + '\n' +
        tesla.printBytes(this.bufferSlotsStartPtr, 100) + "\n<->\n" +
        tesla.printBytes(this.bufferSlotsStartPtr+deflatedSize-50, 50) + '\n'
      */
      val inflatedSize = Snappy.rawUncompress(this.bufferSlotsStartPtr, this.deflatedSize, destination)
      inflatedSize
    } catch safely {
      case t: Throwable =>
        log error(burstStdMsg(s"COULD_NOT_INFLATE $this  $tag", t), t)
        throw t
    }
  }

  @inline final override
  def inflateFrom(source: TeslaParcel): Unit = {
    lazy val tag = s"TeslaParcelInflator.inflateFrom"
    if (source.isInflated)
      throw VitalsException(s"ALREADY_INFLATED sourceParcel=$source  $this")
    if (this.maxAvailableMemory < inflatedSize)
      throw VitalsException(s"$this size=${this.maxAvailableMemory} is insufficient neededSize=${source.inflatedSize}")
    if (source == this)
      throw VitalsException(s"sourceParcel=$source cannot be the same $this")

    this.reset
    this.bufferCount(source.bufferCount)
    this.inflatedSize(source.inflatedSize)
    this.deflatedSize(source.deflatedSize)
    this.isInflated(true)
    this.currentUsedMemory(this.inflatedSize)
    val sz = source.inflateTo(bufferSlotsStartPtr)
    if (sz != source.inflatedSize)
      throw VitalsException(s"sourceParcel=$source inflatedSize=${source.inflatedSize} doesn't agree with realInflatedSize=$sz $this")
  }

}
