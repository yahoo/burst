/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2

import org.burstsys.tesla.TeslaTypes._

package object key {
  final val nullMapFieldOffset: TeslaMemoryOffset = 0 // LONG

  final val dimCountFieldOffset: TeslaMemoryOffset = nullMapFieldOffset + SizeOfLong // BYTE

  final val dimensionValuesOffset: TeslaMemoryOffset = dimCountFieldOffset + SizeOfByte

}
