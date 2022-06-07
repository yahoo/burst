/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.tesla
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.zap.cube.ZapCube
import org.xerial.snappy.Snappy

/**
 * ==Serialization and Deserialization==
 * ...
 * ==very simple state==
 * The total state of a cube is in a long row size value, a single block of off-heap memory (byte array), and a
 * brio dictionary.
 * ==sparse and redundant array==
 * This off heap memory is fairly sparse (lots of zeros) since we use all longs to store data, and have null maps etc,
 * and it seems there should be a lot of duplicated numbers for various domain specific reasons. We believe that
 * snappy compression of this type of byte array should yield a very high compression ratio.
 */
trait ZapCubeCodec extends Any with ZapCube {

  def lastRow_=(s: Int): Unit

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   */
  final
  def write(k: Kryo, out: Output): Unit = {
    try {
      out writeInt lastRow
      out writeBoolean rowLimited
      val actualMemorySize: Int = (rowSize * rowCount) + bucketBlockSize
      val destinationBlock = tesla.block.factory grabBlock (rowSize * rowCount) + bucketBlockSize
      try {
        val compressedSize = Snappy.rawCompress(
          cubeDataStart, actualMemorySize, destinationBlock.dataStart
        )
        out writeLong compressedSize
        var i = 0
        while (i < compressedSize) {
          out writeByte tesla.offheap.getByte(destinationBlock.dataStart + i)
          i += 1
        }
      } finally tesla.block.factory releaseBlock destinationBlock
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  final
  def read(k: Kryo, in: Input): Unit = {
    try {
      lastRow = in.readInt
      rowLimited = in.readBoolean
      val compressedSize = in.readLong
      val compressedBlock = tesla.block.factory grabBlock compressedSize.toInt
      try {
        var i = 0
        while (i < compressedSize) {
          tesla.offheap.putByte(compressedBlock.dataStart + i, in.readByte)
          i += 1
        }
        Snappy.rawUncompress(compressedBlock.dataStart, compressedSize, cubeDataStart)
      } finally tesla.block.factory releaseBlock compressedBlock
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
