/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.wheel

import org.burstsys.tesla.TeslaTypes._

package object state {
  final val poolIdFieldOffset: TeslaMemoryOffset = 0 // Int

  final val wheelSpokeCountOffset: TeslaMemoryOffset = poolIdFieldOffset + SizeOfInteger

  final val wheelSpokesStartOffset: TeslaMemoryOffset = wheelSpokeCountOffset + SizeOfInteger

}
