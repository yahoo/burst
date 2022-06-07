/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.static

import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.dictionary.container.{BrioDictionaryReadAccessor, BrioDictionaryState}
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.text.VitalsTextCodec


/**
  * A static dictionary is an immutable read only version of the standard dictionary used where
  * data is in a static fixed form such as fabric data cache disk encodings.
  */
final case class BrioStaticDictionaryAnyVal(ptr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with BrioDictionary with BrioDictionaryState with BrioDictionaryReadAccessor {

  override def blockPtr: TeslaMemoryPtr = ??? // TODO

  @inline override
  def currentMemorySize: TeslaMemoryOffset = tesla.offheap.getInt(ptr)

  @inline override
  def basePtr: TeslaMemoryPtr = ptr + SizeOfInteger

  @inline override
  def checkPtr(p: TeslaMemoryPtr): TeslaMemoryPtr = {
    if (p > ptr + currentMemorySize)
      throw VitalsException(s"bad ptr p=$p, ptr=$ptr, size=$currentMemorySize ")
    p
  }

  override def keyLookupWithAdd(string: String)(implicit text: VitalsTextCodec): BrioDictionaryKey =
    throw VitalsException(s"not applicable to static dictionary")

}
