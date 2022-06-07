/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.blob

import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.lattice.BrioLatticeReference
import org.burstsys.brio.types.BrioTypes.{BrioDictionaryKey, _}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, _}
import org.burstsys.tesla.buffer.TeslaBufferReader
import org.burstsys.vitals.text.VitalsTextCodec

final case class BrioEmptyBlob(sliceKey: Int) extends BrioBlob {

  def dataPtr: TeslaMemoryPtr = TeslaNullMemoryPtr

  /**
   * operation to release resources for this blob
   */
  override def close: BrioBlob = this

  /**
   * The brio encoded object tree data
   */
  @inline
  override def data: TeslaBufferReader = TeslaEmptyBufferAnyVal()

  /**
   * The string dictionary for this blob
   */
  @inline
  override def dictionary: BrioDictionary = BrioEmptyDictionaryAnyVal()

  /**
   * The root reference
   */
  @inline
  override def reference: BrioLatticeReference = BrioLatticeReference()

  /**
   * The byte size of this blob
   */
  override def size: TeslaMemoryPtr = 0

  /**
   * @return true, this blob is a marker for no data
   */
  override def isEmpty: Boolean = true
}

final case class EmptySliceAnyVal(sliceKey: Int) extends AnyVal with BrioSlice

final case class TeslaEmptyBufferAnyVal(dummy: Boolean = false) extends AnyVal with TeslaBufferReader {

  override def readBoolean(index: TeslaMemoryOffset): Boolean = ???

  override def readBooleans(index: TeslaMemoryOffset, length: TeslaMemorySize): Array[Boolean] = ???

  override def readBooleanInArray(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize, value: Boolean): Boolean = ???

  override def readByte(absolutePosition: TeslaMemoryOffset): Byte = ???

  override def readBytes(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize): Array[Byte] = ???

  override def readByteInArray(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize, value: Byte): Boolean = ???

  override def readShort(absolutePosition: TeslaMemoryOffset): Short = ???

  override def readShorts(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize): Array[Short] = ???

  override def readShortInArray(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize, value: Short): Boolean = ???

  override def readInteger(absolutePosition: TeslaMemoryOffset): TeslaMemorySize = ???

  override def readIntegers(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize): Array[TeslaMemorySize] = ???

  override def readIntegerInArray(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize, value: TeslaMemorySize): Boolean = ???

  override def readLong(absolutePosition: TeslaMemoryOffset): TeslaMemoryPtr = ???

  override def readLongs(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize): Array[TeslaMemoryPtr] = ???

  override def readLongInArray(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize, value: TeslaMemoryPtr): Boolean = ???

  override def readDouble(absolutePosition: TeslaMemoryOffset): Double = ???

  override def readDoubles(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize): Array[Double] = ???

  override def readDoubleInArray(absolutePosition: TeslaMemoryOffset, length: TeslaMemorySize, value: Double): Boolean = ???

  override def readOffset(absolutePosition: TeslaMemoryOffset): TeslaMemoryOffset = ???

  override def checkPtr(ptr: TeslaMemoryPtr): TeslaMemoryPtr = TeslaEndMarkerMemoryPtr

  override def dataPtr: TeslaMemoryPtr = TeslaNullMemoryPtr

  override def maxAvailableMemory: TeslaMemorySize = 0

  override def currentUsedMemory: TeslaMemorySize = 0
}

final case class BrioEmptyDictionaryAnyVal(dummy: Boolean = false) extends AnyVal with BrioDictionary {

  override def keyLookup(string: String)(implicit text: VitalsTextCodec): BrioDictionaryKey = ???

  override def stringLookup(key: BrioDictionaryKey)(implicit text: VitalsTextCodec): String = ???

  override def dump(implicit text: VitalsTextCodec): String = "<Empty Dictionary>"

  override def keySet(implicit text: VitalsTextCodec): Array[BrioDictionaryKey] = ???

  override def words: TeslaMemorySize = 0

  override def overflowed: Boolean = false

  override def keyOverflowed: Boolean = false

  override def slotOverflowed: Boolean = false

  override def keyLookupWithAdd(string: String)(implicit text: VitalsTextCodec): BrioDictionaryKey = BrioDictionaryNotFound
}
