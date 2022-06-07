/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.{SizeOfLong, TeslaMemoryPtr}
import org.burstsys.tesla.offheap
import org.burstsys.zap.cube
import org.burstsys.zap.cube.{ZapCubeRow, _}

/**
 * We store rows as a linear array of buckets addressed by a hash of the row dimensions. Each bucket points
 * to the first in a bucket-list of rows that all have the same hash for their dimensions.  Each row has a
 * __link__ column that is a pointer to the next row or [[ZapCube]] as a value
 */
trait ZapCubeBuckets extends Any with ZapCube {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def bucket(builder: ZapCubeBuilder, thisCube: ZapCubeContext, index: Int): ZapMemoryOffset =
    thisCube.getLong(bucketOffset(index))

  @inline final
  def setBucket(builder: ZapCubeBuilder, thisCube: ZapCubeContext, index: Int, value: ZapMemoryOffset): Unit = {
    thisCube.putLong(bucketOffset(index), value)
  }

  @inline final
  def hash(builder: ZapCubeBuilder, thisCube: ZapCubeContext, key: FabricDataKeyAnyVal): Int =
    Math.abs(key.hashcode(thisCube.cursorKeyLength) % thisCube.bucketCount)

  /**
   *
   * @param startAddress
   */
  @inline final
  def initBuckets(startAddress: TeslaMemoryPtr): Unit = {
    if (false) { // for some reason there is suspicion that this is slower...
      tesla.offheap.setMemory(startAddress, bucketCount * SizeOfLong, 0)
    } else {
      var i = 0
      while (i < bucketCount) {
        offheap.putLong(startAddress + (i * 8), ZapCubeEmptyBucket)
        i += 1
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline private[zap] final
  def bucketHead(builder: ZapCubeBuilder, thisCube: ZapCubeContext, index: Int): ZapCubeRow =
    cube.ZapCubeRow(bucket(builder, thisCube, index))

  @inline private[zap] final
  def bucketOffset(index: Int): ZapMemoryOffset = {
    val offset = bucketBlockOffset + (index * BucketFieldSize)
    //    checkOffset(offset)
    offset
  }

}
