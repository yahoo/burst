/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.plane

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.fabric.wave.execution.model.gather.plane.FabricPlane
import org.burstsys.felt.model.collectors.route.runtime.FeltRouteFactory
import org.burstsys.felt.model.collectors.route.{FeltRouteBuilder, FeltRouteCollector}
import org.burstsys.felt.model.collectors.runtime.{FeltCollectorPlane, FeltCollectorPlaneContext}
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize

/**
 * A [[FabricPlane]] for route collectors
 */
trait FeltRoutePlane extends FeltCollectorPlane[FeltRouteBuilder, FeltRouteCollector]

object FeltRoutePlane {

  def apply(): FeltRoutePlane = FeltRoutePlaneContext()

}

final case
class FeltRoutePlaneContext()
  extends FeltCollectorPlaneContext[FeltRouteBuilder, FeltRouteCollector]
    with FeltRoutePlane with FeltRoutePlaneMerge with FeltRouteFactory {

  override def grabCollector(builder: FeltRouteBuilder, desiredSize: TeslaMemorySize): FeltRouteCollector =
    planeBinding.collectors.routes.grabCollector(builder, desiredSize)

  override def releaseCollector(collector: FeltRouteCollector): Unit =
    planeBinding.collectors.routes.releaseCollector(collector)

  override def newBuilder(): FeltRouteBuilder = planeBinding.collectors.routes.newBuilder

  override def rowLimitExceeded: Boolean = false

  override def rowCount: Int = 0

  override def planeDictionary: BrioMutableDictionary = null

  override def planeDictionary_=(d: BrioMutableDictionary): Unit = {
  }

  override def dictionaryOverflow: Boolean = false
}
