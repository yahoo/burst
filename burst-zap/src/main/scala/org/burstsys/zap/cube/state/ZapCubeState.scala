/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.block.TeslaBlockAnyVal
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.cube
import org.burstsys.zap.cube._

/**
 * All cube operations related to direct manipulation of the off heap memory
 */
trait ZapCubeState extends AnyRef with ZapCube {

  override def currentMemorySize: TeslaMemoryOffset = ??? // UNUSED IN CUBES

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // internal state
  //////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  val keyData = new Array[Long](64)

  final override
  val cursorKey: FabricDataKeyAnyVal = FabricDataKeyAnyVal(new Array[Long](64))

  final
  var cursorKeyLength: Int = 0

  final
  var cursorUpdated: Boolean = true

  @inline final
  var cursorRow: ZapCubeRow = cube.ZapCubeRow()

  @inline override
  def rowCount_=(count: Int): Unit = lastRow = count - 1

  @inline override
  def rowCount: Int = lastRow + 1

  final
  var bucketCount: Int = _

  final override
  var rowLimited: Boolean = false

  final
  var rowSize: TeslaMemorySize = 0

  final
  var bucketBlockSize: TeslaMemorySize = 0

  /**
   * This is the last row that  has been allocated. -1 means that there are no rows.
   */
  final
  var lastRow: Int = ZapCubeZeroRows

  /**
   * the start of the rows as an offset from the beginning of memory
   *
   * @return
   */
  final
  var rowBlockOffset: ZapMemoryOffset = 0 // schema.bucketBlockSize

  @inline final override
  def freshKeyData: Array[Long] = {
    var d = 0
    while (d < keyData.length) {
      keyData(d) = 0L
      d += 1
    }
    keyData
  }

  @inline final override
  def isEmpty: Boolean = rowCount == 0

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // misc state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Here is where we allocate both the bucket and row memory at the same time. This is __relocatable__ so we
   * can export and import the compressed/inflated memory block over the network and still have all the
   * wiring work.
   */
  final override
  def cubeDataStart: TeslaMemoryPtr = TeslaBlockAnyVal(blockPtr).dataStart

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This is the fixed bucket set. Each bucket consists of a __relative__ pointer to the memory address of the
   * first row in the bucket list. This goes into the beginning of the off heap memory block.
   */
  final
  def bucketBlockAddress: TeslaMemoryPtr = cubeDataStart

  /**
   * This is the offset from the start of our allocated memory where the rows storage starts.
   * This is __relocatable__.
   */
  @inline private[zap] final
  def rowBlockAddress(thisCube: ZapCubeContext): TeslaMemoryPtr = cubeDataStart + thisCube.rowBlockOffset

  /**
   *
   * @return
   */
  @inline private[zap] final
  def endOfMemory(builder: ZapCubeBuilder): Long = cubeDataStart + builder.totalMemorySize

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Memory checks
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * guard routine to check to see if row is in memory block
   *
   * @param index
   */
  @inline private[zap] final
  def checkRow(builder: ZapCubeBuilder, index: Int): Unit = {
    if (index >= builder.rowLimit)
      throw VitalsException(f" index($index)is outside limit(${builder.rowLimit}%,d)")
  }

  /**
   * Guard routine to see if offset is in memory block
   *
   * @param offset
   */
  @inline private[zap] final
  def checkOffset(builder: ZapCubeBuilder, offset: ZapMemoryOffset): Unit = {
    if (offset >= builder.totalMemorySize) {
      //      cube.log info printCube
      throw VitalsException(f" offset($offset)is outside memory block($cubeDataStart%,d, ${endOfMemory(builder)}%,d)")
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Memory Read/Write -- all reads and writes go through here to manage relative address re-allocation of memory
  // everything else is offsets from the start of the memory block
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline private[zap] final
  def putLong(offset: TeslaMemoryPtr, value: Long): Unit = {
    tesla.offheap.putLong(cubeDataStart + offset, value)
  }

  @inline private[zap] final
  def getLong(offset: TeslaMemoryPtr): Long = {
    tesla.offheap.getLong(cubeDataStart + offset)
  }

}
