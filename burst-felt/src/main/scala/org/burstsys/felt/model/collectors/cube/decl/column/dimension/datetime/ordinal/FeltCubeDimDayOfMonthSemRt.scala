/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.ordinal

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive

final case
class FeltCubeDimDayOfMonthSemRt() extends FeltCubeDimTimeOrdinalSemRt {
  semanticType = DAY_OF_MONTH_ORDINAL_DIMENSION_SEMANTIC

  @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = dayOfMonthOrdinal(time)
}

