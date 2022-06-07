/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
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

  /**
   *
   */
  private[cube] final
  val zapCubeDefaultDimension = 0L


  ///////////////////////////////////////////////////////////
  // Layout
  ///////////////////////////////////////////////////////////

  /**
   *
   */
  private[cube] final
  val zapCubeDefaultAggregation = 0L

  /**
   * There must be at least one root cube
   */
  final
  val ZapDefaultFrameId: Int = 0

  private[cube] final
  val ZapCubeEmptyLink: ZapMemoryOffset = 0L

  private[cube] final
  val ZapCubeEmptyBucket: TeslaMemoryPtr = 0L

  private[cube] final
  val ZapCubeZeroRows: Int = -1

  private[cube] final
  val BucketFieldSize: TeslaMemorySize = 8

  private[cube] final
  val DimensionNullMapSize: TeslaMemorySize = 8

  private[cube] final
  val AggregationNullMapSize: TeslaMemorySize = 8

  private[cube] final
  val DimensionColumnSize: TeslaMemorySize = 8

  private[cube] final
  val AggregationColumnSize: TeslaMemorySize = 8

  private[cube] final
  val LinkColumnSize: TeslaMemorySize = 8

  /**
   * A relative 64 bit offset from the start of the block of off heap memory
   */
  private[cube] final
  type ZapMemoryOffset = Long

  private[cube] final
  type ZapMemorySize = Long

}
