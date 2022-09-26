/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.ordinal

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive

final case
class FeltCubeDimSecondOfMinuteSemRt() extends FeltCubeDimTimeOrdinalSemRt {
  semanticType = SECOND_OF_MINUTE_ORDINAL_DIMENSION_SEMANTIC

  @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = secondOfMinuteOrdinal(time)
}

