/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valset.flex

import org.burstsys.felt.model.mutables.valset.FeltMutableValSet
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexProxy, TeslaFlexProxyContext, TeslaFlexSlotIndex}
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.mutable.valset
import org.burstsys.zap.mutable.valset.{ZapValSet, ZapValSetBuilder}

/**
 * implementation of the Flex value set mutable
 *
 * @see [[FeltMutableValSet]]
 */
trait ZapFlexValSet extends Any with ZapValSet with TeslaFlexProxy[ZapValSetBuilder, ZapValSet]

final case
class ZapFlexValSetAnyVal(index: TeslaFlexSlotIndex) extends AnyVal with ZapFlexValSet
  with TeslaFlexProxyContext[ZapValSetBuilder, ZapValSet, ZapFlexValSet] {

  override def coupler: TeslaFlexCoupler[ZapValSetBuilder, ZapValSet, ZapFlexValSet] = valset.flex.coupler

  /////////////////////////////////////////////////////////////////////////////////////////////
  // simple delegation for internal dictionary read only methods
  /////////////////////////////////////////////////////////////////////////////////////////////

  override def currentMemorySize: TeslaMemorySize = internalCollector.currentMemorySize

  override def initialize(pId: TeslaPoolId, builder: ZapValSetBuilder): Unit = internalCollector.initialize(pId, builder)

  override def reset(builder: ZapValSetBuilder): Unit = internalCollector.reset(builder)

  override def defaultBuilder: ZapValSetBuilder = internalCollector.defaultBuilder

  override def builder: ZapValSetBuilder = internalCollector.builder

  override def clear(): Unit = internalCollector.clear()

  override def blockPtr: TeslaMemoryPtr = internalCollector.blockPtr

  override def poolId: TeslaPoolId = internalCollector.poolId

  /////////////////////////////////////////////////////////////////////////////////////////////
  // write method(s) that require overflow interception and resizing - just one so far
  /////////////////////////////////////////////////////////////////////////////////////////////

  /////////////////////////////////////////////////////////////////////////////////////////////
  // methods that should not be called on a proxy
  /////////////////////////////////////////////////////////////////////////////////////////////

  override def importCollector(sourceCollector: ZapValSet, sourceItems: TeslaPoolId, builder: ZapValSetBuilder): Unit =
    throw VitalsException(s"can't call this on a flex proxy!")

}
