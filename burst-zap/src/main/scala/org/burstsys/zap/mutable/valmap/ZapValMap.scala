/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valmap

import org.burstsys.felt.model.mutables.valmap.FeltMutableValMap
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaNullMemoryPtr}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.flex.TeslaFlexCollector
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.mutable.valmap.state.ZapValMapState

trait ZapValMap extends Any with FeltMutableValMap with TeslaBlockPart
  with TeslaFlexCollector[ZapValMapBuilder, ZapValMap] {

}

final case
class ZapValMapAnyVal(blockPtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with ZapValMap with ZapValMapState {

  override def importCollector(sourceCollector: ZapValMap, sourceItems: Int, builder: ZapValMapBuilder): Unit = ???

  override def defaultBuilder: ZapValMapBuilder = throw VitalsException(s"default builder not allowed")

  override def builder: ZapValMapBuilder = ???

}
