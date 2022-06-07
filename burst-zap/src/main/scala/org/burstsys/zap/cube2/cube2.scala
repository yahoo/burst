/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.tesla.TeslaTypes.TeslaMemoryPtr

package object cube2 {

  final val partName: String = "cube2"


  final
  val ZapCube2EmptyBucket: TeslaMemoryPtr = 0L

  /**
   *
   */
  trait ZapCube2DimensionAxis extends Any {

    /**
     * the count of dimensions
     *
     * @return
     */
    def dimCount: Int

    /**
     * nullity for a zero based dimension
     *
     * @param dimension
     * @return
     */
    def dimIsNull(dimension: Int): Boolean

    /**
     *
     * @param dimension
     */
    def dimSetNotNull(dimension: Int): Unit

    /**
     * set a zero based dimension to null
     *
     * @param dimension
     * @return
     */
    def dimSetNull(dimension: Int): Unit

    /**
     * write a zero based dimension value
     *
     * @param dimension
     * @param value
     */
    def dimWrite(dimension: Int, value: BrioPrimitive): Unit

    /**
     * read from a zero based dimension
     *
     * @param dimension
     * @return
     */
    def dimRead(dimension: Int): BrioPrimitive

  }

  /**
   *
   */
  trait ZapCube2AggregationAxis extends Any {

    /**
     * count of aggregations
     *
     * @return
     */
    def aggCount: Int

    /**
     * nullity for a zero based aggregation
     *
     * @param aggregation
     * @return
     */
    def aggIsNull(aggregation: Int): Boolean

    /**
     * set a zero based aggregation to null
     *
     * @param aggregation
     */
    def aggSetNull(aggregation: Int): Unit

    /**
     * set a zero based aggregation to not null
     *
     * @param aggregation
     */
    def aggSetNotNull(aggregation: Int): Unit

    /**
     * write to a zero based aggregation
     *
     * @param aggregation
     * @param value
     */
    def aggWrite(aggregation: Int, value: BrioPrimitive): Unit

    /**
     * read from a zero based aggregation
     *
     * @param aggregation
     * @return
     */
    def aggRead(aggregation: Int): BrioPrimitive

  }

}
