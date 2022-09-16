/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap

import org.burstsys.vitals.reporter.VitalsByteQuantReporter

package object cube {

  final val partName: String = "cube"

  object ZapCubeReporter extends VitalsByteQuantReporter("zap", "cube")

  /**
   * The ordinal of a cube's dimension within a row
   */
  type ZapCubeDimensionKey = Int

  /**
   * The ordinal of a cube's aggregation within a row
   */
  type ZapCubeAggregationKey = Int

  /**
   *
   */
  final
  val ZapCubeInvalidAggregationKey = 0

  /**
   *
   */
  final
  val ZapCubeInvalidDimensionKey = 0

}
