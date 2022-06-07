/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.felt.model.collectors.tablet.FeltTabletCollector
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaNullMemoryPtr}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.tablet.state.ZapTabletState

trait ZapTablet extends Any with FeltTabletCollector with TeslaBlockPart {

  def tabletSize: Int

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

  override def currentMemorySize: TeslaMemoryOffset = ??? // TODO

  ////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = {
  }

  override def read(kryo: Kryo, input: Input): Unit = {
  }

  override def rowCount: TeslaPoolId = 0

  override def rowCount_=(count: TeslaPoolId): Unit = {
  }

  override def rowLimited: Boolean = false

  override def rowLimited_=(s: Boolean): Unit = {
  }

  override def clear(): Unit = {
  }

  override def isEmpty: Boolean = true

}
