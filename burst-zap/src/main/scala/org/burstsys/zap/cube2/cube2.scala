/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap

import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.vitals.reporter.VitalsByteQuantReporter

package object cube2 {

  final val partName: String = "cube2"

  object ZapCube2Reporter extends VitalsByteQuantReporter("zap", "cube2")

  /**
   *
   */
  trait ZapCube2DimensionAxis extends Any {

    /**
     * the count of dimensions
     */
    def dimCount: Int

    /**
     * nullity for a zero based dimension
     */
    def dimIsNull(dimension: Int): Boolean

    /**
     */
    def dimSetNotNull(dimension: Int): Unit

    /**
     * set a zero based dimension to null
     */
    def dimSetNull(dimension: Int): Unit

    /**
     * write a zero based dimension value
     */
    def dimWrite(dimension: Int, value: BrioPrimitive): Unit

    /**
     * read from a zero based dimension
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
     */
    def aggIsNull(aggregation: Int): Boolean

    /**
     * set a zero based aggregation to null
     */
    def aggSetNull(aggregation: Int): Unit

    /**
     * set a zero based aggregation to not null
     */
    def aggSetNotNull(aggregation: Int): Unit

    /**
     * write to a zero based aggregation
     */
    def aggWrite(aggregation: Int, value: BrioPrimitive): Unit

    /**
     * read from a zero based aggregation
     */
    def aggRead(aggregation: Int): BrioPrimitive
  }

}
