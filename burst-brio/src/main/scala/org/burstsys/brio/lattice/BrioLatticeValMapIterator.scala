/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.lattice

import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.types.BrioTypes.{BrioDictionaryKey, SizeOfString}
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.TeslaBufferReader
import org.burstsys.vitals.text.VitalsTextCodec

/**
 * Zero GC iterator for Value Maps
 */
final case
class BrioLatticeValMapIterator(startOffset: TeslaMemoryOffset = TeslaNullOffset) extends AnyVal {

  @inline 
  def length(reader: TeslaBufferReader): Int = reader.readShort(startOffset)

  @inline 
  def readBooleanKey(ordinal: Int, reader: TeslaBufferReader): Boolean = {
    reader.readBoolean(startOffset + SizeOfShort + (ordinal * SizeOfBoolean))
  }

  @inline 
  def readByteKey(ordinal: Int, reader: TeslaBufferReader): Byte = {
    reader.readByte(startOffset + SizeOfShort + (ordinal * SizeOfByte))
  }

  @inline 
  def readShortKey(ordinal: Int, reader: TeslaBufferReader): Short = {
    reader.readShort(startOffset + SizeOfShort + (ordinal * SizeOfShort))
  }

  @inline 
  def readIntKey(ordinal: Int, reader: TeslaBufferReader): Int = {
    reader.readInteger(startOffset + SizeOfShort + (ordinal * SizeOfInteger))
  }

  @inline 
  def readLongKey(ordinal: Int, reader: TeslaBufferReader): Long = {
    reader.readLong(startOffset + SizeOfShort + (ordinal * SizeOfLong))
  }

  @inline 
  def readDoubleKey(ordinal: Int, reader: TeslaBufferReader): Double = {
    reader.readDouble(startOffset + SizeOfShort + (ordinal * SizeOfDouble))
  }

  @inline 
  def readStringKey(ordinal: Int, reader: TeslaBufferReader)
                   (text: VitalsTextCodec, dictionary: BrioDictionary): String = {
    val key = reader.readShort(startOffset + SizeOfShort + (ordinal * SizeOfString))
    dictionary.stringLookup(key)(text)
  }

  @inline 
  def readLexiconStringKey(ordinal: Int, reader: TeslaBufferReader): BrioDictionaryKey = {
    reader.readShort(startOffset + SizeOfShort + (ordinal * SizeOfShort))
  }

  @inline 
  def readLongStringValue(ordinal: Int, reader: TeslaBufferReader)
                         (text: VitalsTextCodec, dictionary: BrioDictionary): String = {
    val key = reader.readShort(startOffset + SizeOfShort + (length(reader) * SizeOfLong) + (ordinal * SizeOfString))
    dictionary.stringLookup(key)(text)
  }

  @inline 
  def readStringStringValue(ordinal: Int, reader: TeslaBufferReader)
                           (text: VitalsTextCodec, dictionary: BrioDictionary): String = {
    val key = reader.readShort(startOffset + SizeOfShort + (length(reader) * SizeOfString) + (ordinal * SizeOfString))
    dictionary.stringLookup(key)(text)
  }

  @inline 
  def readLexiconStringStringValue(ordinal: Int, reader: TeslaBufferReader): BrioDictionaryKey = {
    reader.readShort(
      startOffset + SizeOfShort + (length(reader) * SizeOfString) + (ordinal * SizeOfString)
    )
  }

  // TODO more VALUE READING COMBINATIONS

}
