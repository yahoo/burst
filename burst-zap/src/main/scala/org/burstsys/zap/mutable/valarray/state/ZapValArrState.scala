/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valarray.state

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.offheap
import org.burstsys.tesla.pool._
import org.burstsys.zap.mutable.valarray.{ZapValArr, ZapValArrayBuilder}

/**
 */
trait ZapValArrState extends Any with ZapValArr {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Pool Id
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def poolId: TeslaPoolId = offheap.getInt(basePtr + poolIdFieldOffset)

  @inline final
  def poolId_=(id: TeslaPoolId): Unit = offheap.putInt(basePtr + poolIdFieldOffset, id)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def initialize(pId: TeslaPoolId, builder: ZapValArrayBuilder): Unit = {
    poolId = pId
    reset(builder)
  }

  @inline final override
  def reset(builder: ZapValArrayBuilder): Unit = {
  }

  @inline final override
  def clear(): Unit = {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // flex support
  //////////////////////////////////////////////////////////////////////////////////////////////////

  final override def currentMemorySize: TeslaMemorySize = availableMemorySize // TODO

}
