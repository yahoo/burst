/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.mutable

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary.container.{BrioDictionaryReadAccessor, BrioDictionaryState, BrioDictionaryWriteAccessor}
import org.burstsys.brio.dictionary.flex.BrioDictionaryBuilder
import org.burstsys.brio.dictionary.{BrioDictionary, defaultDictionaryBuilder}
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.flex.TeslaFlexCollector
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.text.VitalsTextCodec

trait BrioMutableDictionary extends Any with BrioDictionary with TeslaBlockPart
  with TeslaFlexCollector[BrioDictionaryBuilder, BrioMutableDictionary] {

  final override def defaultBuilder: BrioDictionaryBuilder = defaultDictionaryBuilder

  final override def builder: BrioDictionaryBuilder = defaultBuilder

  /**
   * mark an overflow
   *
   * @deprecated use key or slot versions
   */
  def flagOverflow(): Unit

  /**
   * flag key exhaustion overflow
   */
  def flagKeyOverflow(): Unit

  /**
   * flag slot (size) exhaustion overflow
   */
  def flagSlotOverflow(): Unit

  /**
   * the size for a serialized write/read
   *
   * @return
   */
  def serializationSize: TeslaMemorySize

  /**
   * kryo write
   *
   * @param k
   * @param out
   */
  def write(k: Kryo, out: Output): Unit

  /**
   * kryo read
   *
   * @param k
   * @param in
   */
  def read(k: Kryo, in: Input): Unit

}


final case
class BrioMutableDictionaryAnyVal(blockPtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with BrioMutableDictionary with BrioDictionaryState with BrioDictionaryReadAccessor
  with BrioDictionaryWriteAccessor {

  /// support for flex dictionaries
  override def importCollector(sourceCollector: BrioMutableDictionary, sourceItems: TeslaPoolId, builder: BrioDictionaryBuilder): Unit =
    importDictionary(sourceCollector, sourceItems)

  override def toString: String = {
    if (blockPtr == TeslaNullMemoryPtr)
      s"NULL"
    else
      this.dump(VitalsTextCodec())
  }

}
