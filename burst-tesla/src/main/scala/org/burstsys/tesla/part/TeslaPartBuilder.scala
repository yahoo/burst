/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.part

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize

/**
 * All [[TeslaPart]] instances implement this to tell their [[TeslaPartShop]] how to allocate
 * as well as initialize...
 */
trait TeslaPartBuilder extends Any {

  def defaultStartSize: TeslaMemorySize

  def multiplier: Double = 2

  final def chooseSize(size: TeslaMemorySize): TeslaMemorySize =
    if (size == teslaBuilderUseDefaultSize) defaultStartSize else size
}
