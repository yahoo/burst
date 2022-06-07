/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.ginsu.functions.GinsuFunctions

/**
 * Slice time into partitions - NOTE this does not return new numbers but new UTC dates (longs/ticks)
 * that are of a smaller sampling
 */

trait FeltCubeDimDatetimeSemRt extends FeltCubeDimSemRt
  /*with VitalsKryoStatelessSerializable*/ with GinsuFunctions {

  final override val dimensionHandlesStrings: Boolean = false

  @inline def sliceTime(time: Long)(implicit threadRuntime: BrioThreadRuntime): Long

  @inline final override def doLong(value: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = sliceTime(value)

}
