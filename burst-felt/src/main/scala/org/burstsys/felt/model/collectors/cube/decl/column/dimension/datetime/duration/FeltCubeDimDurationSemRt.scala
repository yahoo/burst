/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.duration

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.ginsu.functions.datetime.GinsuDurationFunctions
import org.burstsys.vitals.kryo.VitalsKryoStatelessSerializable

/**
 * Slice time into partitions - NOTE this does not return new numbers but new UTC dates (longs/ticks)
 * that are of a smaller sampling
 */
trait FeltCubeDimDurationSemRt extends AnyRef
  with FeltCubeDimSemRt with VitalsKryoStatelessSerializable with GinsuDurationFunctions {
  override protected val dimensionHandlesStrings: Boolean = false

  @inline final override
  def doLong(value: Long)(implicit threadRuntime: BrioThreadRuntime): BrioPrimitive = duration(value)

  @inline def duration(time: Long)(implicit threadRuntime: BrioThreadRuntime): Long
}

