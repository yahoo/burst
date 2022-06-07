/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.flex

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary.defaultDictionaryBuilder
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexProxy, TeslaFlexProxyContext, TeslaFlexSlotIndex}
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.text.VitalsTextCodec

/**
  * a dictionary that can ''grow'' using tesla flex collector framework.
  * Its used during merges with other dictionaries where
  * the growth feature is more important than its use of real objects. It wraps a
  * regular mutable dictionary and allows for replacement when that dictionary runs out of room.
  * all mutations are through `keyLookupWithAdd()`
  * <br/>'''NOTE:''' the allocation and freeing of a single instance is ''not'' sync'ed. i.e.
  * you must ensure that there are not multiple threads doing grab and release for the same dictionary.
  * Read access to a allocated dictionary ''is'' reentrant however. As always updates to a dictionary
  * are ''not'' thread-safe. This is roughly the same as normal dictionaries.
  */
trait BrioFlexDictionary extends Any with BrioMutableDictionary with TeslaFlexProxy[BrioDictionaryBuilder, BrioMutableDictionary]

final case
class BrioFlexDictionaryAnyVal(index: TeslaFlexSlotIndex) extends AnyVal with BrioFlexDictionary
  with TeslaFlexProxyContext[BrioDictionaryBuilder, BrioMutableDictionary, BrioFlexDictionary] {

  override def toString: String = internalCollector.toString

  override def coupler: TeslaFlexCoupler[BrioDictionaryBuilder, BrioMutableDictionary, BrioFlexDictionary] = org.burstsys.brio.dictionary.flex.coupler

  /////////////////////////////////////////////////////////////////////////////////////////////
  // simple delegation for internal dictionary read only methods
  /////////////////////////////////////////////////////////////////////////////////////////////

  override def overflowed: Boolean = internalCollector.overflowed

  override def keyOverflowed: Boolean = internalCollector.keyOverflowed

  override def slotOverflowed: Boolean = internalCollector.slotOverflowed

  override def words: Int = internalCollector.words

  override def dump(implicit text: VitalsTextCodec): String = internalCollector.dump

  override def keySet(implicit text: VitalsTextCodec): Array[BrioDictionaryKey] = internalCollector.keySet

  override def stringLookup(key: BrioDictionaryKey)(implicit text: VitalsTextCodec): String = internalCollector.stringLookup(key)

  override def keyLookup(string: String)(implicit text: VitalsTextCodec): BrioDictionaryKey = internalCollector.keyLookup(string)

  override def currentMemorySize: TeslaMemoryOffset = internalCollector.currentMemorySize

  override def poolId: TeslaPoolId = internalCollector.poolId

  override def blockPtr: TeslaMemoryPtr = internalCollector.blockPtr

  override def write(k: Kryo, out: Output): Unit = internalCollector.write(k, out)

  override def read(k: Kryo, in: Input): Unit = internalCollector.read(k, in)

  override def flagOverflow(): Unit = internalCollector.flagOverflow()

  override def flagKeyOverflow(): Unit = internalCollector.flagKeyOverflow()

  override def flagSlotOverflow(): Unit = internalCollector.flagSlotOverflow()

  override def serializationSize: TeslaMemorySize = internalCollector.serializationSize

  /////////////////////////////////////////////////////////////////////////////////////////////
  // write method(s) that require overflow interception and resizing - just one so far
  /////////////////////////////////////////////////////////////////////////////////////////////

  @scala.annotation.tailrec
  override def keyLookupWithAdd(string: String)(implicit text: VitalsTextCodec): BrioDictionaryKey = {
    val words = internalCollector.words
    val key = internalCollector.keyLookupWithAdd(string)
    if (internalCollector.slotOverflowed) {
      coupler.upsize(this.index, words, defaultDictionaryBuilder)
      // we have an opportunity to redo this
      keyLookupWithAdd(string)
    } else key
  }

  override def importCollector(sourceCollector: BrioMutableDictionary, sourceItems: TeslaPoolId, builder: BrioDictionaryBuilder): Unit = {
    throw VitalsException(s"resize should not be called in flex dictionary!")
  }

  override def initialize(id: TeslaPoolId, builder:BrioDictionaryBuilder): Unit = {
    throw VitalsException(s"initialize should not be called in flex dictionary!")
  }

  override def reset(builder:BrioDictionaryBuilder): Unit = {
    throw VitalsException(s"reset should not be called in flex dictionary!")
  }

}
