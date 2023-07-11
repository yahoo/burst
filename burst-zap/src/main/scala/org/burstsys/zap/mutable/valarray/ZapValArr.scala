/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valarray

import org.burstsys.felt.model.mutables.valarr.FeltMutableValArr
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize, TeslaNullMemoryPtr}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.flex.TeslaFlexCollector
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.mutable.valarray.state.ZapValArrState

/**
 * implementation of the Flex value array mutable
 *
 * @see [[FeltMutableValArr]]
 */
trait ZapValArr extends Any with FeltMutableValArr with TeslaBlockPart
  with TeslaFlexCollector[ZapValArrayBuilder, ZapValArr] {

}

final case
class ZapValArrayAnyVal(blockPtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with ZapValArr with ZapValArrState {

  override def importCollector(sourceCollector: ZapValArr, sourceItems: Int, builder: ZapValArrayBuilder): Unit = ???

  override def defaultBuilder: ZapValArrayBuilder = throw VitalsException(s"default builder not allowed")

  override def builder: ZapValArrayBuilder = ???

  override def itemCount: Int = ???

  override def size(): TeslaMemorySize = ???

  override def itemLimited: Boolean = ???
}
