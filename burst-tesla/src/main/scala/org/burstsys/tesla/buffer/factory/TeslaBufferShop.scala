/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer.factory


import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.buffer
import org.burstsys.tesla.buffer.TeslaBufferBuilder
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.part.TeslaPartShop

/**
  * a tesla buffer part shop
  */
trait TeslaBufferShop extends TeslaPartShop[TeslaMutableBuffer, TeslaBufferBuilder] {

  def partName: String = buffer.partName

  /**
    * grab a tesla buffer
    *
    * @param size
    * @return
    */
  def grabBuffer(size: TeslaMemorySize): TeslaMutableBuffer

  /**
    * release a tesla buffer
    *
    * @param buffer
    */
  def releaseBuffer(buffer: TeslaMutableBuffer): Unit

}
