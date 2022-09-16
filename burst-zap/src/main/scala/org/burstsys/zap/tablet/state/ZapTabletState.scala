/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet.state


import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.offheap
import org.burstsys.tesla.pool._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.tablet.{ZapTablet, ZapTabletBuilder}

/**
 * This Universal trait manages all off heap state associated with the
 * [[org.burstsys.zap.tablet.ZapTabletAnyVal]] value class.
 * Note that we use a linear scan instead of a binary search of some sort for
 * tablet membership tests. Since tablets in the real world tend to be quite small
 * this is tractable. If they start getting larger we would need to evaluate better.
 */
trait ZapTabletState extends Any  with ZapTablet {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Pool Id
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def poolId: TeslaPoolId = offheap.getInt(basePtr + poolIdFieldOffset)

  @inline final
  def poolId_=(id: TeslaPoolId): Unit = offheap.putInt(basePtr + poolIdFieldOffset, id)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Tablet Size
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def tabletSize: Int = offheap.getInt(basePtr + tabletSizeFieldOffset)

  @inline final
  def tabletSize_=(size: Int): Unit = offheap.putInt(basePtr + tabletSizeFieldOffset, size)

  @inline final
  def incrementTabletSize(): Unit = {
    offheap.putInt(basePtr + tabletSizeFieldOffset, tabletSize + 1)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Tablet Size
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def tabletItemSize: TeslaMemorySize = offheap.getInt(basePtr + tabletItemSizeFieldOffset)

  @inline final
  def tabletItemSize_=(size: TeslaMemorySize): Unit = offheap.putInt(basePtr + tabletItemSizeFieldOffset, size)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Tablet Limited
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def tabletLimited: Boolean = {
    offheap.getInt(basePtr + tabletLimitedFieldOffset) != 0
  }

  @inline final
  def tabletLimited_=(limited: Boolean): Unit = {
    offheap.putInt(basePtr + tabletLimitedFieldOffset,{
      if (limited)
        1
      else
        0
    })
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // indexed access
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def tabletBooleanAt(i: Int): Boolean = {
    offheap.getByte(checkPtr(basePtr + tabletDataFieldOffset + (i * SizeOfBoolean))) > 0
  }

  @inline final override
  def tabletByteAt(i: Int): Byte = {
    offheap.getByte(checkPtr(basePtr + tabletDataFieldOffset + (i * SizeOfByte)))
  }

  @inline final override
  def tabletShortAt(i: Int): Short = {
    offheap.getShort(checkPtr(basePtr + tabletDataFieldOffset + (i * SizeOfShort)))
  }

  @inline final override
  def tabletIntegerAt(i: Int): Int = {
    offheap.getInt(checkPtr(basePtr + tabletDataFieldOffset + (i * SizeOfInteger)))
  }

  @inline final override
  def tabletLongAt(i: Int): Long = {
    offheap.getLong(checkPtr(basePtr + tabletDataFieldOffset + (i * SizeOfLong)))
  }

  @inline final override
  def tabletDoubleAt(i: Int): Double = {
    offheap.getDouble(checkPtr(basePtr + tabletDataFieldOffset + (i * SizeOfDouble)))
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // adds
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private def tabletAddItem(itemSize: TeslaMemorySize): TeslaMemoryOffset = {
    val lastSize = tabletItemSize
    if (lastSize == 0) {
      tabletItemSize = itemSize
    } else if (lastSize != itemSize) {
      throw VitalsException("tablet recorded item size differs from operation item size")
    }
    val offset = tabletDataFieldOffset + tabletSize*itemSize
    if (offset + itemSize > availableMemorySize) {
      tabletLimited = true
    } else {
      incrementTabletSize()
    }
    offset
  }

  @inline final override
  def tabletAddBoolean(value: Boolean): Unit = {
    val offset = tabletAddItem(SizeOfBoolean)
    if (!itemLimited)
      offheap.putByte(basePtr + offset, if (value) 1 else 0)
  }

  @inline final override
  def tabletAddByte(value: Byte): Unit = {
    val offset = tabletAddItem(SizeOfByte)
    if (!itemLimited)
      offheap.putByte(basePtr + offset, value)
  }

  @inline final override
  def tabletAddShort(value: Short): Unit = {
    val offset = tabletAddItem(SizeOfShort)
    if (!itemLimited)
      offheap.putShort(basePtr + offset, value)
  }

  @inline final override
  def tabletAddInteger(value: Int): Unit = {
    val offset = tabletAddItem(SizeOfInteger)
    if (!itemLimited)
      offheap.putInt(basePtr + offset, value)
  }

  @inline final override
  def tabletAddLong(value: Long): Unit = {
    val offset = tabletAddItem(SizeOfLong)
    if (!itemLimited)
      offheap.putLong(basePtr + offset, value)
  }

  @inline final override
  def tabletAddDouble(value: Double): Unit = {
    val offset = tabletAddItem(SizeOfDouble)
    if (!itemLimited)
      offheap.putDouble(basePtr + offset, value)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // contains
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def tabletContainsBoolean(value: Boolean): Boolean = {
    var i = 0
    while (i < tabletSize) {
      if (tabletBooleanAt(i) == value) return true
      i += 1
    }
    false
  }

  @inline final override
  def tabletContainsByte(value: Byte): Boolean = {
    var i = 0
    while (i < tabletSize) {
      if (tabletByteAt(i) == value) return true
      i += 1
    }
    false
  }

  @inline final override
  def tabletContainsShort(value: Short): Boolean = {
    var i = 0
    while (i < tabletSize) {
      if (tabletShortAt(i) == value) return true
      i += 1
    }
    false
  }

  @inline final override
  def tabletContainsInteger(value: Int): Boolean = {
    var i = 0
    while (i < tabletSize) {
      if (tabletIntegerAt(i) == value) return true
      i += 1
    }
    false
  }

  @inline final override
  def tabletContainsLong(value: Long): Boolean = {
    var i = 0
    while (i < tabletSize) {
      if (tabletLongAt(i) == value) return true
      i += 1
    }
    false
  }

  @inline final override
  def tabletContainsDouble(value: Double): Boolean = {
    var i = 0
    while (i < tabletSize) {
      if (tabletDoubleAt(i) == value) return true
      i += 1
    }
    false
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * done once at creation/allocation time
   *
   * @return
   */
  @inline
  def initialize(id: TeslaPoolId): ZapTablet = {
    poolId=id
    reset
  }

  /**
   * this done each time the route is re-used.
   *
   * @return
   */
  @inline
  def reset: ZapTablet = {
    tabletSize=0
    tabletItemSize = 0
    itemLimited = false
    this
  }

  override def importCollector(sourceCollector: ZapTablet, sourceItems: TeslaPoolId, builder: ZapTabletBuilder): Unit = {
    val localPoolId = this.poolId
    if (sourceCollector.availableMemorySize > this.availableMemorySize) {
      throw VitalsException("import to a smaller collector")
    }
    tesla.offheap.copyMemory(sourceCollector.basePtr, this.basePtr, sourceCollector.currentMemorySize)
    this.poolId = localPoolId
    this.tabletLimited = false
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Debugging
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def toString: String =
    s"""ZapTablet(
       |  tabletSize=$tabletSize
       |  tabletItemSize=$tabletItemSize =
       |  poolId=$poolId
       |)""".stripMargin


}
