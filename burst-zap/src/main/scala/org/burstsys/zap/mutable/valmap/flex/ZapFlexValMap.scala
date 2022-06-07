/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valmap.flex

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexProxy, TeslaFlexProxyContext, TeslaFlexSlotIndex}
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.mutable.valmap
import org.burstsys.zap.mutable.valmap.{ZapValMap, ZapValMapBuilder}

/**
 *
 */
trait ZapFlexValMap extends Any with ZapValMap with TeslaFlexProxy[ZapValMapBuilder, ZapValMap]


final case
class ZapFlexValMapAnyVal(index: TeslaFlexSlotIndex) extends AnyVal with ZapFlexValMap
  with TeslaFlexProxyContext[ZapValMapBuilder, ZapValMap, ZapFlexValMap] {

  override def coupler: TeslaFlexCoupler[ZapValMapBuilder, ZapValMap, ZapFlexValMap] = valmap.flex.coupler


  /////////////////////////////////////////////////////////////////////////////////////////////
  // simple delegation for internal dictionary read only methods
  /////////////////////////////////////////////////////////////////////////////////////////////

  override def currentMemorySize: TeslaMemorySize = internalCollector.currentMemorySize

  override def initialize(pId: TeslaPoolId, builder: ZapValMapBuilder): Unit = internalCollector.initialize(pId, builder)

  override def reset(builder: ZapValMapBuilder): Unit = internalCollector.reset(builder)

  override def defaultBuilder: ZapValMapBuilder = internalCollector.defaultBuilder

  override def builder: ZapValMapBuilder = internalCollector.builder

  override def clear(): Unit = internalCollector.clear()

  override def blockPtr: TeslaMemoryPtr = internalCollector.blockPtr

  override def poolId: TeslaPoolId = internalCollector.poolId

  /////////////////////////////////////////////////////////////////////////////////////////////
  // write method(s) that require overflow interception and resizing - just one so far
  /////////////////////////////////////////////////////////////////////////////////////////////

  /////////////////////////////////////////////////////////////////////////////////////////////
  // methods that should not be called on a proxy
  /////////////////////////////////////////////////////////////////////////////////////////////
  override def importCollector(sourceCollector: ZapValMap, sourceItems: TeslaPoolId, builder: ZapValMapBuilder): Unit =
    throw VitalsException(s"can't call this on a flex proxy!")
}
