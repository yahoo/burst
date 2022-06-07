/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.wheel

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaNullMemoryPtr}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.wheel.state.ZapWheelState

trait ZapWheel extends Any with TeslaBlockPart {

  /**
   * TODO
   *
   * @return
   */
  def spokeCount: Int

  def initialize(id: TeslaPoolId): ZapWheel

  def reset: ZapWheel
}


final case
class ZapWheelAnyVal(blockPtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with ZapWheel with ZapWheelState {

  override def currentMemorySize: TeslaMemoryOffset = ??? // TODO


}
