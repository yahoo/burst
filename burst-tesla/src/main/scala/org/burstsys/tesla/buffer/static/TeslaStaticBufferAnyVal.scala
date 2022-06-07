/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer.static

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.TeslaBufferReader
import org.burstsys.tesla.buffer.access.TeslaBufferReadAccessors
import org.burstsys.vitals.errors.VitalsException

/**
 * Static Buffers are read only simplified versions of [[org.burstsys.tesla.buffer.TeslaBuffer]].
 * They are immutable (no writer interface) and must be
 * prefixed by an Int 'size'. They are meant to be returned by
 * a simple pointer to a place in a mmap region and
 * be destroyed when the mmap region is let go
 */
final case
class TeslaStaticBufferAnyVal(basePtr: TeslaMemoryPtr) extends AnyVal
  with TeslaBufferReader with TeslaBufferReadAccessors {

  ////////////////////////////////////////////////////////////////
  // bounds checking
  ////////////////////////////////////////////////////////////////

  @inline override
  def checkPtr(ptr: TeslaMemoryPtr): TeslaMemoryPtr = {
    val maxMem = currentUsedMemory
    // remember to account for the 4 byte size field at the beginning of the buffer
    val delta = (basePtr + maxMem + SizeOfInteger) - ptr
    if (ptr > basePtr + maxMem + SizeOfInteger) {
      val msg = s"bad memory reference basePtr=$basePtr: ptr=$ptr,  limitMem=${
        basePtr + maxMem
      } -- off by ${-delta} byte(s)"
      throw VitalsException(msg)
    }
    ptr
  }

  ////////////////////////////////////////////////////////////////
  // first field in the memory block is the count of data size in bytes
  ////////////////////////////////////////////////////////////////

  @inline override
  def currentUsedMemory: TeslaMemorySize = tesla.offheap.getInt(basePtr)

  @inline override
  def maxAvailableMemory: TeslaMemorySize = currentUsedMemory

  ////////////////////////////////////////////////////////////////
  // this is where client data goes
  ////////////////////////////////////////////////////////////////

  @inline
  def dataPtr: TeslaMemoryPtr = basePtr + SizeOfInteger

}
