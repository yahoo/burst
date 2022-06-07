/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.lattice

import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.types.BrioTypes.{BrioTypeKey, brioDataTypeByteSize}
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.TeslaBufferReader
import org.burstsys.tesla.offheap
import org.burstsys.vitals.text.VitalsTextCodec

/**
 * Zero GC iterator for Value Vectors
 */
final case
class BrioLatticeValVecIterator(startOffset: TeslaMemoryOffset = TeslaNullOffset) extends AnyVal {

  @inline
  def length(reader: TeslaBufferReader): Int = reader.readShort(startOffset)

  @inline
  def start(reader: TeslaBufferReader): TeslaMemoryOffset = {
    startOffset + org.burstsys.brio.types.BrioTypes.SizeOfCount
  }

  @inline
  def advance(reader: TeslaBufferReader, offset: TeslaMemoryOffset, bType: BrioTypeKey): TeslaMemoryOffset = {
    offset + brioDataTypeByteSize(bType)
  }

  @inline
  def readBoolean(reader: TeslaBufferReader, offset: TeslaMemoryOffset): Boolean = {
    reader.readBoolean(offset)
  }

  @inline
  def readByte(reader: TeslaBufferReader, offset: TeslaMemoryOffset): Byte = {
    reader.readByte(offset)
  }

  @inline
  def readShort(reader: TeslaBufferReader, offset: TeslaMemoryOffset): Short = {
    reader.readShort(offset)
  }

  @inline
  def readInteger(reader: TeslaBufferReader, offset: TeslaMemoryOffset): Int = {
    reader.readInteger(offset)
  }

  @inline
  def readLong(reader: TeslaBufferReader, offset: TeslaMemoryOffset): Long = {
    reader.readLong(offset)
  }

  @inline
  def readDouble(reader: TeslaBufferReader, offset: TeslaMemoryOffset): Double = {
    reader.readDouble(offset)
  }

  @inline
  def readString(reader: TeslaBufferReader, offset: TeslaMemoryOffset)
                (codec: VitalsTextCodec, dictionary: BrioDictionary): String = {
    dictionary.stringLookup(offheap.getShort(offset))(codec)
  }
}
