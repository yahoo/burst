/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.grain

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive

final case class FeltCubeDimSecondGrainSemRt() extends FeltCubeDimTimeGrainSemRt {
  semanticType = SECOND_GRAIN_DIMENSION_SEMANTIC

  @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
    secondGrain(time)
}

