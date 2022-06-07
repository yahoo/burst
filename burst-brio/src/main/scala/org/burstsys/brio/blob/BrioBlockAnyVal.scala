/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.blob

import org.burstsys
import org.burstsys.tesla.TeslaTypes._


/**
  * A single Fabric Item from a segment file captured in a scala value class.
  * This is an objectless wrapper around a chunk of off-heap memory.
  */
final case
class BrioBlockAnyVal(ptr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal {

  /**
    * stored in the header is the size of this block
    *
    * @return
    */
  def itemSize: TeslaMemorySize = burstsys.tesla.offheap.getInt(ptr)

  /**
    * Header Size + Data Size
    *
    * @return
    */
  def totalBlockSize: TeslaMemorySize = itemSize + SizeOfInteger

  /**
    * This is the start of the Brio Blob.
    *
    * @return
    */
  def itemStartPtr: TeslaMemoryPtr = ptr + SizeOfInteger

  /**
    * the next block after this one. You need to check this against the size of the file
    * because the only way of knowing if there are more blocks is to check where
    * you are against the total segment file size
    *
    * @return
    */
  def nextBlock: BrioBlockAnyVal = BrioBlockAnyVal(itemStartPtr + itemSize)

}
