/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.slot

import org.burstsys.brio.dictionary.container._
import org.burstsys.brio.types.BrioTypes.{BrioDictionaryKey, BrioWordSize}
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.text.VitalsTextCodec

/**
  * The memory wrapper for each of the string slots in the  dictionary off heap memory block.
  *
  * @param ptr
  */
final case
class BrioDictionarySlotValue(ptr: TeslaMemoryPtr = TeslaNullOffset) extends AnyVal {

  //////////////////////////////////////////////////////////////////////////////////
  // lifecycle routines
  //////////////////////////////////////////////////////////////////////////////////

  @inline
  def initialize: BrioDictionarySlotValue = {
    link(0)
    stringSize(0.toShort)
    this
  }

  //////////////////////////////////////////////////////////////////////////////////
  // The key of the slot
  //////////////////////////////////////////////////////////////////////////////////

  @inline private[dictionary]
  def key: BrioDictionaryKey = tesla.offheap.getShort(ptr)

  @inline private[dictionary]
  def key(k: BrioDictionaryKey): Unit = tesla.offheap.putShort(ptr, k)

  //////////////////////////////////////////////////////////////////////////////////
  // The link to the next slot
  //////////////////////////////////////////////////////////////////////////////////

  @inline private[dictionary]
  def linkStart: TeslaMemoryPtr = ptr + SizeOfShort

  @inline private[dictionary]
  def link: TeslaMemoryOffset = tesla.offheap.getInt(linkStart)

  @inline private[dictionary]
  def link(size: TeslaMemoryOffset): Unit = tesla.offheap.putInt(linkStart, size)

  //////////////////////////////////////////////////////////////////////////////////
  // The size in bytes of the contained string
  //////////////////////////////////////////////////////////////////////////////////

  @inline private[dictionary]
  def stringSizeStart: TeslaMemoryPtr = linkStart + SizeOfInteger

  @inline private[dictionary]
  def stringSize: BrioWordSize = tesla.offheap.getShort(stringSizeStart)

  @inline private[dictionary]
  def stringSize(size: BrioWordSize): Unit = tesla.offheap.putShort(stringSizeStart, size)

  //////////////////////////////////////////////////////////////////////////////////
  // the actual string data
  //////////////////////////////////////////////////////////////////////////////////

  @inline private[dictionary]
  def stringDataStart: TeslaMemoryPtr = stringSizeStart + SizeOfShort

  @inline private[dictionary]
  def stringData(s: String)(implicit text: VitalsTextCodec): Unit = {
    val bytes = text.encode(s)
    stringSize(bytes.length.toShort)
    var i = 0
    while (i < bytes.length) {
      tesla.offheap.putByte(stringDataStart + i, bytes(i))
      i += 1
    }
    link(NullSlotOffset)
  }

  //////////////////////////////////////////////////////////////////////////////////
  // The size in the total slot in bytes
  //////////////////////////////////////////////////////////////////////////////////

  @inline private[dictionary]
  def slotSize: TeslaMemorySize = (stringDataStart + stringSize - ptr).toInt

  //////////////////////////////////////////////////////////////////////////////////
  // String operations
  //////////////////////////////////////////////////////////////////////////////////

  @inline
  def asString(implicit text: VitalsTextCodec): String = {
    val data = new Array[Byte](stringSize)
    var i = 0
    while (i < data.length) {
      data(i) = tesla.offheap.getByte(stringDataStart + i)
      i += 1
    }
    text.decode(data)
  }

  @inline
  def stringMatches(bytes: Array[Byte]): Boolean = {
    if (bytes.length == 0 && stringSize == 0.toShort) return true
    val sSize = stringSize.toInt
    if (bytes.length != sSize) return false
    var i = 0
    while (i < sSize) {
      if (bytes(i) != tesla.offheap.getByte(stringDataStart + i))
        return false
      i += 1
    }
    true
  }

}
