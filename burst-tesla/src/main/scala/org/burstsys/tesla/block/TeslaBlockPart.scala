/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.block

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr}
import org.burstsys.tesla.part.TeslaPart
import org.burstsys.tesla.pool.TeslaPooledResource
import org.burstsys.vitals.errors.VitalsException

/**
 * common routines used by all parts that use the [[TeslaBlock]] as an underlying memory framework
 */
trait TeslaBlockPart extends Any with TeslaPart with TeslaPooledResource {

  /**
   * this is a point to the memory location that is the absolute block base of whatever underlying memory
   * is allocated for this part. For parts that are embedded into [[TeslaBlock]] instances that
   * is the pointer to the based on that block. If they are not, then this is the same as the
   * `basePtr`
   */
  def blockPtr: TeslaMemoryPtr

  /**
   * confirm a pointer is valid
   */
  @inline
  def checkPtr(ptr: TeslaMemoryPtr): TeslaMemoryPtr =
    TeslaBlockAnyVal(blockPtr).checkPtr(ptr) // use the any val so we don't create a new object

  @inline final
  def blockBasePtr: TeslaMemoryPtr =
    TeslaBlockAnyVal(blockPtr).blockBasePtr // use the any val so we don't create a new object

  /**
   * @return the start of the data section of the underlying memory block
   */
  @inline
  def basePtr: TeslaMemoryPtr =
    TeslaBlockAnyVal(blockPtr).dataStart // use the any val so we don't create a new object

  /**
   * @return the memory size allocated in the underlying tesla block
   */
  @inline final
  def availableMemorySize: TeslaMemoryOffset =
    TeslaBlockAnyVal(blockPtr).dataSize // use the any val so we don't create a new object

  /**
   * this is the current memory actually used in the part
   */
  def currentMemorySize: TeslaMemoryOffset

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // reference counting
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def validateAndIncrementReferenceCount(msg: String = ""): Unit = {
    val block = TeslaBlockAnyVal(blockPtr)
    if (block.referenceCount >= TeslaPart.MAX_PERMISSIBLE_REFS) {
      throw VitalsException(s"ATTEMPTED_EXCESS_GRAB referenceCount=$referenceCount $msg")
    }
    block.validateAndIncrementReferenceCount(msg)
  }

  @inline final override
  def validateAndDecrementReferenceCount(msg: String = ""): Unit = {
    val block = TeslaBlockAnyVal(blockPtr)
    if (block.referenceCount < 0) {
      throw VitalsException(s"BROKEN_REFERENCE_COUNT referenceCount=$referenceCount $msg")
    } else if (block.referenceCount == 0) {
      throw VitalsException(s"ATTEMPTED_EXCESS_FREE referenceCount=$referenceCount $msg")
    }
    block.validateAndDecrementReferenceCount(msg)
  }

  @inline final override
  def referenceCount: Int =
    TeslaBlockAnyVal(blockPtr).referenceCount // use the any val so we don't create a new object

}
