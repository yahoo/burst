/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.internal

import org.burstsys.tesla.TeslaTypes.TeslaMemoryPtr
import org.burstsys.tesla.parcel.state.TeslaParcelState
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.parcel.log
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.safely
import org.xerial.snappy.Snappy

/**
  * compression
  */
trait TeslaParcelDeflator extends Any with TeslaParcel with TeslaParcelState {

  @inline final override
  def deflateTo(destination: TeslaMemoryPtr): Long = {
    if (!isInflated)
      throw VitalsException(s"already deflated $this")
    try {
      val deflatedSize = Snappy.rawCompress(this.bufferSlotsStartPtr, this.currentUsedMemory, destination)
      /*
      log info s"deflateTo() currentUsedMemory=$currentUsedMemory, deflatedSize=$deflatedSize $this" + '\n' +
        tesla.printBytes(destination, 100) + "\n<->\n" +
        tesla.printBytes(destination+deflatedSize-50, 50) + '\n'
        */
      deflatedSize
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"could not compress $this", t)
        throw t
    }
  }

  @inline final override
  def deflateFrom(source: TeslaParcel): Unit = {
    if (!source.isInflated)
      throw VitalsException(s"$this already deflated")
    if (this.maxAvailableMemory < source.inflatedSize)
      throw VitalsException(s"size=${this.maxAvailableMemory} is insufficient neededSize=${source.inflatedSize} $this")
    if (source == this)
      throw VitalsException(s"sourceParcel=$source cannot be the same $this")

    this.reset
    this.bufferCount(source.bufferCount)
    this.inflatedSize(source.inflatedSize)
    this.isInflated(false)
    val dSize = source.deflateTo(this.bufferSlotsStartPtr)
    this.deflatedSize(dSize.toInt)
    this.currentUsedMemory(dSize.toInt)
  }

}
