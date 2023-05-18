/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.block

import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.factory.TeslaBlockPool
import org.burstsys.tesla.offheap
import org.burstsys.tesla.part.{TeslaPart, maxPoolsPerPart}
import org.burstsys.tesla.pool.{TeslaPoolId, TeslaPooledResource}
import org.burstsys.vitals.errors.VitalsException

/**
 * A base layer for a series of parts that rely on a common set of block memory management primitives
 */
trait TeslaBlock extends Any with TeslaPart with TeslaPooledResource {

  /**
   *
   * @return
   */
  def dataSize: TeslaMemorySize

  /**
   *
   * @return
   */
  def dataStart: TeslaMemoryPtr

  /**
   *
   * @return
   */
  def blockBasePtr: TeslaMemoryPtr

  /**
   *
   * @param testPtr
   * @return
   */
  def checkPtr(testPtr: TeslaMemoryPtr): TeslaMemoryPtr

  /**
   *
   * @param bytesSize
   * @param pool
   * @return
   */
  def initialize(bytesSize: TeslaMemorySize, pool: TeslaPoolId): TeslaBlock
}

/*
object TeslaBlock {

  def apply(blockBasePtr: TeslaMemoryPtr = TeslaNullMemoryPtr): TeslaBlock =
    TeslaBlockAnyVal(blockBasePtr)

}
*/

/**
 * A underlying common binary memory management mechanism used for various other tesla ''parts''.
 * A block is a part of a [[TeslaBlockPool]]. It has a header and a memory section.
 * {{{
 *   | HEADER        | 12 bytes
 *   | MEMORY BLOCK  | Array[Byte] variable size
 * }}}
 * The 12 byte header consists of :
 * {{{
 *   | POOL_ID          | Integer (4 bytes) fixed size
 *   | REFERENCE_COUNT  | Integer (4 bytes) fixed size
 *   | DATA_SIZE        | Integer (4 bytes) fixed size
 * }}}
 * a block is limited to a size of less than 2,147,483,647 bytes (2GB)
 */
final case
class TeslaBlockAnyVal(blockBasePtr: TeslaMemoryPtr = TeslaNullMemoryPtr)
  extends AnyVal with TeslaBlock {

  ///////////////////////////////////////////////////////////////////////////
  // PoolId
  ///////////////////////////////////////////////////////////////////////////

  /**
   * the start of the pool ID field as a memory pointer (64 bit LONG)
   *
   * @return
   */
  @inline private
  def poolIdStart: TeslaMemoryPtr = blockBasePtr

  /**
   * the value of the pool id
   *
   * @return
   */
  @inline override
  def poolId: TeslaPoolId = offheap.getInt(poolIdStart)

  @inline
  def poolId(id: TeslaPoolId): Unit = offheap.putInt(poolIdStart, id)

  ///////////////////////////////////////////////////////////////////////////
  // Reference Count
  ///////////////////////////////////////////////////////////////////////////

  /**
   * the start of the reference count field as a memory pointer (64 bit LONG)
   *
   * @return
   */
  @inline private
  def referenceCountStart: TeslaMemoryPtr = poolIdStart + SizeOfInteger

  @inline override
  def validateAndIncrementReferenceCount(msg: String = ""): Unit = {
    val count = offheap.getInt(referenceCountStart)
    if (count >= TeslaPart.MAX_PERMISSIBLE_REFS) {
      throw VitalsException(s"BLOCK_OVERCOMMIT refrenceCount=$count $msg")
    }
    offheap.putInt(referenceCountStart, count + 1)
  }

  @inline override
  def validateAndDecrementReferenceCount(msg: String = ""): Unit = {
    val count = offheap.getInt(referenceCountStart)
    if (count <= 0) {
      throw VitalsException(s"DISCARD_EMPTY_BLOCK referenceCount=$count $msg")
    }
    offheap.putInt(referenceCountStart, count - 1)
  }

  /**
   * the reference count value used in a generalized block reference tracking system
   *
   * @return
   */
  @inline override
  def referenceCount: Int = offheap.getInt(referenceCountStart)

  @inline
  def referenceCount(count: Int): Unit = offheap.putInt(referenceCountStart, count)

  ///////////////////////////////////////////////////////////////////////////
  // Size
  ///////////////////////////////////////////////////////////////////////////

  /**
   * the start of the data size field as a memory pointer (64 bit LONG)
   *
   * @return
   */
  @inline private
  def dataSizeStart: TeslaMemoryPtr = referenceCountStart + SizeOfInteger

  /**
   * the size of the data section of this block (does not include the header)
   *
   * @return
   */
  @inline override
  def dataSize: TeslaMemorySize = offheap.getInt(dataSizeStart)

  @inline
  def dataSize(size: TeslaMemorySize): Unit = offheap.putInt(dataSizeStart, size)

  ///////////////////////////////////////////////////////////////////////////
  // Data
  ///////////////////////////////////////////////////////////////////////////

  /**
   * the start of the data values block as a memory pointer (64 bit LONG)
   * i.e. this is the start of the variable size opaque byte block after the block header
   *
   * @return
   */
  @inline override
  def dataStart: TeslaMemoryPtr = dataSizeStart + SizeOfInteger

  @inline override
  def checkPtr(testPtr: TeslaMemoryPtr): TeslaMemoryPtr = {
    if (blockBasePtr < 0) {
      throw VitalsException(s"bad block ptr reason=negative block=$blockBasePtr ptr=$testPtr")
    }
    if (testPtr < 0) {
      throw VitalsException(s"bad offset ptr reason=negative block=$blockBasePtr ptr=$testPtr")
    }
    val limitMem = blockBasePtr + dataSize
    if (testPtr > limitMem) {
      throw VitalsException(s"bad offset ptr reason=overrun block=$blockBasePtr ptr=$testPtr limitMem=$limitMem referenceCount=$referenceCount")
    }
    testPtr
  }

  ///////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////////////////////

  @inline override
  def initialize(bytesSize: TeslaMemoryOffset, pool: TeslaPoolId): TeslaBlock = {
    if (pool > maxPoolsPerPart || pool < 0)
      throw VitalsException(s"poolId must be between 0 and $maxPoolsPerPart $poolId")
    poolId(pool)
    dataSize(bytesSize - SizeofBlockHeader)
    referenceCount(0)
    this
  }

}
