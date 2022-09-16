/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.plane

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.fabric.execution.model.gather.plane.FabricPlane
import org.burstsys.felt.model.collectors.runtime.{FeltCollectorPlane, FeltCollectorPlaneContext}
import org.burstsys.felt.model.collectors.tablet.runtime.FeltTabletFactory
import org.burstsys.felt.model.collectors.tablet.{FeltTabletBuilder, FeltTabletCollector}
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize

/**
 * A [[FabricPlane]] for tablet collectors
 */
trait FeltTabletPlane extends FeltCollectorPlane[FeltTabletBuilder, FeltTabletCollector]

/**
 * Felt Version of Fabric Plane
 */
final case
class FeltTabletPlaneContext()
  extends FeltCollectorPlaneContext[FeltTabletBuilder, FeltTabletCollector]
    with FeltTabletPlane with FeltTabletPlaneMerge with FeltTabletFactory {

  def grabCollector(builder: FeltTabletBuilder, desiredSize: TeslaMemorySize): FeltTabletCollector =
    planeBinding.collectors.tablets.grabCollector(builder, desiredSize)

  def releaseCollector(collector: FeltTabletCollector): Unit =
    planeBinding.collectors.tablets.releaseCollector(collector)

  override def newBuilder(): FeltTabletBuilder = planeBinding.collectors.tablets.newBuilder

  override def rowLimitExceeded: Boolean = false

  override def rowCount: Int = 0

  override def planeDictionary: BrioMutableDictionary = null

  override def planeDictionary_=(d: BrioMutableDictionary): Unit = {
  }

  override def dictionaryOverflow: Boolean = false
}
