/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.blob

import org.burstsys.tesla.TeslaTypes.TeslaMemoryPtr
import org.burstsys.tesla.buffer.factory.releaseBuffer
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer

/**
  * A Zap Blob based on a mutable mem buffer. This is used for unit tests where we are not using a
  * cache slice and need to keep a reference to the buffer.
  *
  * @param buffer
  */
final case
class BrioMutableBlob(buffer: TeslaMutableBuffer) extends AnyRef with BrioAbstractBlob {

  @inline override
  def dataPtr: TeslaMemoryPtr = buffer.dataPtr

  @inline override
  def close: BrioBlob = {
    releaseBuffer(buffer)
    this
  }
}

