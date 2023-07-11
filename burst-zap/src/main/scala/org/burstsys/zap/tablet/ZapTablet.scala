/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.felt.model.collectors.tablet.FeltTabletCollector
import org.burstsys.tesla.TeslaTypes.{SizeOfDouble, TeslaMemoryOffset, TeslaMemoryPtr, TeslaMemorySize, TeslaNullMemoryPtr}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.flex.TeslaFlexCollector
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.tablet.state.{ZapTabletState, tabletDataFieldOffset, tabletSizeFieldOffset}

trait ZapTablet extends Any with FeltTabletCollector with TeslaBlockPart with TeslaFlexCollector[ZapTabletBuilder, ZapTablet]
{

  def tabletSize: Int

  def tabletLimited: Boolean

  def initialize(id: TeslaPoolId): ZapTablet

  def reset: ZapTablet

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // indexed access
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline
  def tabletBooleanAt(i: Int): Boolean

  @inline
  def tabletByteAt(i: Int): Byte

  @inline
  def tabletShortAt(i: Int): Short

  @inline
  def tabletIntegerAt(i: Int): Int

  @inline
  def tabletLongAt(i: Int): Long

  @inline
  def tabletDoubleAt(i: Int): Double

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // adds
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline
  def tabletAddBoolean(value: Boolean): Unit

  @inline
  def tabletAddByte(value: Byte): Unit

  @inline
  def tabletAddShort(value: Short): Unit

  @inline
  def tabletAddInteger(value: Int): Unit

  @inline
  def tabletAddLong(value: Long): Unit

  @inline
  def tabletAddDouble(value: Double): Unit

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // contains
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline
  def tabletContainsBoolean(value: Boolean): Boolean

  @inline
  def tabletContainsByte(value: Byte): Boolean

  @inline
  def tabletContainsShort(value: Short): Boolean

  @inline
  def tabletContainsInteger(value: Int): Boolean

  @inline
  def tabletContainsLong(value: Long): Boolean

  @inline
  def tabletContainsDouble(value: Double): Boolean

}

object ZapTablet {

}

final
case class ZapTabletAnyVal(blockPtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with ZapTabletState with ZapTablet {

  override def currentMemorySize: TeslaMemoryOffset = {
    val size = tabletItemSize
    if (size == 0 && tabletSize != 0) {
      throw VitalsException("items in tablet with no recorded item size")
    }
    tabletDataFieldOffset + tabletSize*tabletItemSize
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = {
  }

  override def read(kryo: Kryo, input: Input): Unit = {
  }

  override def size(): TeslaMemorySize = currentMemorySize

  override def itemCount: TeslaPoolId = {
    tabletSize
  }

  override def itemCount_=(count: TeslaPoolId): Unit = {
    tabletSize = count
  }


  override def itemLimited: Boolean = {
    tabletLimited
  }

  override def itemLimited_=(s: Boolean): Unit = {
    tabletLimited = s
  }

  override def clear(): Unit = {
    tabletLimited = false
    tabletSize = 0
  }

  override def isEmpty: Boolean = {
    tabletSize > 0
  }

  override def initialize(pId: TeslaPoolId, builder: ZapTabletBuilder): Unit = {
    clear()
  }

  override def reset(builder: ZapTabletBuilder): Unit = {
    clear()
  }

  override def defaultBuilder: ZapTabletBuilder = {
    ZapTabletBuilder(SizeOfDouble)
  }

  override def builder: ZapTabletBuilder = {
    ZapTabletBuilder(SizeOfDouble)
  }
}
