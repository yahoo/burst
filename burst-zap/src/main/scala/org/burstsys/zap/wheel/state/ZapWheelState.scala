/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.wheel.state

import org.burstsys.tesla.offheap
import org.burstsys.tesla.pool._
import org.burstsys.zap.wheel.ZapWheel

trait ZapWheelState extends Any with ZapWheel {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Pool Id
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def poolId: TeslaPoolId = offheap.getInt(basePtr + poolIdFieldOffset)

  @inline final
  def poolId(id: TeslaPoolId): Unit = offheap.putInt(basePtr + poolIdFieldOffset, id)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Wheel Size
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def spokeCount: Int = offheap.getInt(basePtr + wheelSpokeCountOffset)

  @inline final
  def spokeCount(size: Int): Unit = offheap.putInt(basePtr + wheelSpokeCountOffset, size)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * done once at creation/allocation time
   *
   * @return
   */
  @inline
  def initialize(id: TeslaPoolId): ZapWheel = {
    poolId(id)
    reset
  }

  /**
   * this done each time the route is re-used.
   *
   * @return
   */
  @inline
  def reset: ZapWheel = {
    this
  }


}
