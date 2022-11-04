/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.plane

import org.burstsys.fabric.wave.execution.model.gather.FabricMerge
import org.burstsys.vitals.errors.safely

trait FeltRoutePlaneMerge extends FeltRoutePlane {

  @inline final override
  def regionMerge(thatResult: FabricMerge): Unit = {
    try {
    } catch safely {
      case t: Throwable =>
        this.markException(t)
    }
  }

  @inline final override
  def sliceMerge(thatResult: FabricMerge): Unit = {
    try {

    } catch safely {
      case t: Throwable =>
        this.markException(t)
    }
  }

  @inline final override
  def waveMerge(thatResult: FabricMerge): Unit = {
    try {

    } catch safely {
      case t: Throwable =>
        this.markException(t)
    }
  }

  @inline final override
  def sliceFinalize(): Unit = {
  }

  @inline final override
  def waveFinalize(): Unit = {

  }

}
