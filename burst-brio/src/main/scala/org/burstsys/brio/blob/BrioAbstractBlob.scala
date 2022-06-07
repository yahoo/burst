/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.blob

import org.burstsys.brio.dictionary.static.BrioStaticDictionaryAnyVal
import org.burstsys.brio.lattice._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.static.TeslaStaticBufferAnyVal
import org.burstsys.tesla.{buffer, offheap}

/**
  * common operations for both mutable and static blobs
 * BE SURE THIS IS A UNIVERSAL TRAIT (extends ANY)
  */
trait BrioAbstractBlob extends Any with BrioBlob {

  def dataPtr: TeslaMemoryPtr

  @inline final override
  def data: TeslaStaticBufferAnyVal = {
    if (dataPtr == TeslaNullMemoryPtr) throw VitalsException(s"dataPtr was null in static blob")
    // skip encoding version and root object version
    var cursor = dataPtr + SizeOfInteger + SizeOfInteger
    val dictionarySize = offheap.getInt(cursor)
    // skip dictionary size field
    cursor += SizeOfInteger
    // skip dictionary data
    cursor += dictionarySize
    // and we have arrived
    TeslaStaticBufferAnyVal(cursor)
  }

  @inline final override
  def dictionary: BrioStaticDictionaryAnyVal = {
    if (dataPtr == TeslaNullMemoryPtr) throw VitalsException(s"dataPtr was null in static blob")
    // skip encoding version and root object version
    val cursor = dataPtr + SizeOfInteger + SizeOfInteger
    BrioStaticDictionaryAnyVal(cursor)
  }

  @inline final override
  def reference: BrioLatticeReference = {
    if (dataPtr == TeslaNullMemoryPtr) throw VitalsException(s"dataPtr was null in static blob")
    BrioLatticeReference(0)
  }

  @inline final override
  def size: Long = {
    if (dataPtr == TeslaNullMemoryPtr) throw VitalsException(s"dataPtr was null in static blob")
    // skip encoding version and root object version
    var cursor = dataPtr + SizeOfInteger + SizeOfInteger
    val dictionarySize = offheap.getInt(cursor)
    // skip dictionary size field
    cursor += SizeOfInteger
    // skip dictionary data
    cursor += dictionarySize
    // and we arrive at object size field
    val objectSize = offheap.getInt(cursor)
    dictionarySize + objectSize
  }
}

