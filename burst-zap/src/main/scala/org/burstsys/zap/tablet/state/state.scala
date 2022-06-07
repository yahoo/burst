/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet

import org.burstsys.tesla.TeslaTypes._


package object state {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Tablet Structure
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val poolIdFieldOffset: TeslaMemoryOffset = 0 // Int

  final val tabletSizeFieldOffset: TeslaMemoryOffset = poolIdFieldOffset + SizeOfInteger

  final val tabletDataFieldOffset: TeslaMemoryOffset = tabletSizeFieldOffset + SizeOfInteger

}
