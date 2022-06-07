/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valset

import org.burstsys.felt.model.mutables.valset.FeltMutableValSet
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaNullMemoryPtr}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.flex.TeslaFlexCollector
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.mutable.valset.state.ZapValSetState

/**
 * implementation of the Flex value set mutable
 *
 * @see [[FeltMutableValSet]]
 */
trait ZapValSet extends Any with FeltMutableValSet with TeslaBlockPart
  with TeslaFlexCollector[ZapValSetBuilder, ZapValSet] {

}

object ZapValSet {

  def apply(): ZapValSet = ZapValSetContext(TeslaNullMemoryPtr)

  def apply(blockPtr: TeslaMemoryPtr): ZapValSet = ZapValSetContext(blockPtr)

}

private final case
class ZapValSetContext(blockPtr: TeslaMemoryPtr) extends AnyVal
  with ZapValSet with ZapValSetState {

  override def importCollector(sourceCollector: ZapValSet, sourceItems: Int, builder: ZapValSetBuilder): Unit = ???

  override def defaultBuilder: ZapValSetBuilder = throw VitalsException(s"default builder not allowed")

  override def builder: ZapValSetBuilder = ???

}
