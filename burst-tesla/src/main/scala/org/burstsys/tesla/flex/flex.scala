/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.vitals.logging._

import scala.language.existentials

package object flex extends VitalsLogger {

  type TeslaFlexSlotIndex = Integer

  /**
   * we need a null pointer for a flex pointer
   */
  final val emptySlotIndex: TeslaFlexSlotIndex = -1
  /**
   * we can mark each empty slot with a negative number
   */
  final val emptySlotValue: TeslaMemoryPtr = -1L

}
