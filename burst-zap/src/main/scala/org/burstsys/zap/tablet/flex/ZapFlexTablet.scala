/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet.flex

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexProxy, TeslaFlexProxyContext, TeslaFlexSlotIndex}
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.tablet
import org.burstsys.zap.tablet.{ZapTablet, ZapTabletBuilder}

/**
 * The Tesla Flex Proxy for the tablet
 */
trait ZapFlexTablet extends Any with ZapTablet with TeslaFlexProxy[ZapTabletBuilder, ZapTablet]

private final case
class ZapFlexTabletAnyVal(index: TeslaFlexSlotIndex) extends AnyVal with ZapFlexTablet
  with TeslaFlexProxyContext[ZapTabletBuilder, ZapTablet, ZapFlexTablet] {

  override def blockPtr: TeslaMemoryPtr =
    internalCollector.blockPtr

  override def poolId: TeslaPoolId =
    internalCollector.poolId

  override def currentMemorySize: TeslaMemorySize =
    internalCollector.currentMemorySize

  /// Proxy
  override def coupler: TeslaFlexCoupler[ZapTabletBuilder, ZapTablet, ZapFlexTablet] =
    tablet.flex.coupler

  override def initialize(id: TeslaPoolId): ZapTablet =
    internalCollector.initialize(id)

  override def reset(builder: ZapTabletBuilder): Unit =
    internalCollector.reset(builder)

  @inline override
  def size(): TeslaMemorySize = internalCollector.size()

  override def itemCount: Int =
    internalCollector.itemCount

  override def itemCount_=(count: Int): Unit = throw new UnsupportedOperationException(s"itemCount_ not allowed")

  override def itemLimited: Boolean =
    internalCollector.itemLimited

  override def itemLimited_=(s: Boolean): Unit = throw new UnsupportedOperationException(s"itemLimited_ not allowed")

  override def isEmpty: Boolean =
    internalCollector.isEmpty

  override def importCollector(sourceCollector: ZapTablet, sourceItems: Int, builder: ZapTabletBuilder): Unit =
    throw VitalsException(s"import collector not allowed")

  override def initialize(pId: TeslaPoolId, builder: ZapTabletBuilder): Unit =
    internalCollector.initialize(pId, builder)

  override def defaultBuilder: ZapTabletBuilder =
    internalCollector.defaultBuilder

  override def builder: ZapTabletBuilder =
    internalCollector.builder

  override def tabletSize: TeslaPoolId =
    internalCollector.tabletSize

  override def tabletLimited: Boolean =
    internalCollector.tabletLimited

  override def reset: ZapTablet =
    internalCollector.reset

  override def clear(): Unit =
    internalCollector.clear()

  override def write(kryo: Kryo, output: Output): Unit =
    internalCollector.write(kryo, output)

  override def read(kryo: Kryo, input: Input): Unit =
    internalCollector.read(kryo, input)

  // at
  override def tabletBooleanAt(i: TeslaPoolId): Boolean =
    internalCollector.tabletBooleanAt(i)

  override def tabletByteAt(i: TeslaPoolId): Byte =
     internalCollector.tabletByteAt(i)

  override def tabletShortAt(i: TeslaPoolId): Short =
     internalCollector.tabletShortAt(i)

  override def tabletIntegerAt(i: TeslaPoolId): TeslaPoolId =
     internalCollector.tabletIntegerAt(i)

  override def tabletLongAt(i: TeslaPoolId): TeslaMemoryPtr =
     internalCollector.tabletLongAt(i)

  override def tabletDoubleAt(i: TeslaPoolId): Double =
     internalCollector.tabletDoubleAt(i)

  // add
  override def tabletAddBoolean(value: Boolean): Unit = {
    internalCollector.tabletAddBoolean(value)
    if (internalCollector.itemLimited) {
      coupler.upsize(this.index, this.availableMemorySize, internalCollector.builder)
      internalCollector.tabletAddBoolean(value)
    }
  }

  override def tabletAddByte(value: Byte): Unit = {
     internalCollector.tabletAddByte(value)
    if (internalCollector.itemLimited) {
      coupler.upsize(this.index, this.availableMemorySize, internalCollector.builder)
      internalCollector.tabletAddByte(value)
    }
  }

  override def tabletAddShort(value: Short): Unit = {
    internalCollector.tabletAddShort(value)
    if (internalCollector.itemLimited) {
      coupler.upsize(this.index, this.availableMemorySize, internalCollector.builder)
      internalCollector.tabletAddShort(value)
    }
  }

  override def tabletAddInteger(value: TeslaPoolId): Unit = {
    internalCollector.tabletAddInteger(value)
    if (internalCollector.itemLimited) {
      coupler.upsize(this.index, this.availableMemorySize, internalCollector.builder)
      internalCollector.tabletAddInteger(value)
    }
  }

  override def tabletAddLong(value: TeslaMemoryPtr): Unit = {
    internalCollector.tabletAddLong(value)
    if (internalCollector.itemLimited) {
      coupler.upsize(this.index, this.availableMemorySize, internalCollector.builder)
      internalCollector.tabletAddLong(value)
    }
  }

  override def tabletAddDouble(value: Double): Unit = {
    internalCollector.tabletAddDouble(value)
    if (internalCollector.itemLimited) {
      coupler.upsize(this.index, this.availableMemorySize, internalCollector.builder)
      internalCollector.tabletAddDouble(value)
    }
  }

  // contains
  override def tabletContainsBoolean(value: Boolean): Boolean =
     internalCollector.tabletContainsBoolean(value)

  override def tabletContainsByte(value: Byte): Boolean =
     internalCollector.tabletContainsByte(value)

  override def tabletContainsShort(value: Short): Boolean =
     internalCollector.tabletContainsShort(value)

  override def tabletContainsInteger(value: TeslaPoolId): Boolean =
     internalCollector.tabletContainsInteger(value)

  override def tabletContainsLong(value: TeslaMemoryPtr): Boolean =
     internalCollector.tabletContainsLong(value)

  override def tabletContainsDouble(value: Double): Boolean =
     internalCollector.tabletContainsDouble(value)

}
