/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2

import org.burstsys.tesla.TeslaTypes._

package object row {

  private[row] final val dimNullMapFieldOffset: TeslaMemoryOffset = 0 // LONG

  private[row] final val aggNullMapFieldOffset: TeslaMemoryOffset = dimNullMapFieldOffset + SizeOfLong // LONG

  private[row] final val dimCountFieldOffset: TeslaMemoryOffset = aggNullMapFieldOffset + SizeOfLong // BYTE

  private[row] final val aggCountFieldOffset: TeslaMemoryOffset = dimCountFieldOffset + SizeOfByte // BYTE

  private[row] final val dirtyFieldOffset: TeslaMemoryOffset = aggCountFieldOffset + SizeOfByte // BYTE

  private[row] final val linkFieldOffset: TeslaMemoryOffset = dirtyFieldOffset + SizeOfByte // INTEGER

  private[row] final val valuesFieldOffset: TeslaMemoryOffset = linkFieldOffset + SizeOfInteger

  /**
   * this is the singleton row returned to indicate that the cube has row limited. This is used
   * to enter a upsize procedure followed by an operation retry
   */
  final val limitExceededMarkerRow: ZapCube2Row = ZapCube2RowAnyVal(TeslaNullMemoryPtr)
}
