/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.duration

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive

final case class FeltCubeDimSecondDurSemRt() extends FeltCubeDimDurationSemRt {
  semanticType = SECOND_DURATION_DIMENSION_SEMANTIC

  @inline override def duration(time: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive =
    secondDuration(time)
}

