/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.shrub.plane

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.fabric.execution.model.gather.plane.FabricPlane
import org.burstsys.felt.model.collectors.runtime.{FeltCollectorPlane, FeltCollectorPlaneContext}
import org.burstsys.felt.model.collectors.shrub.runtime.FeltShrubFactory
import org.burstsys.felt.model.collectors.shrub.{FeltShrubBuilder, FeltShrubCollector}

/**
 * A [[FabricPlane]] for shrubs
 */
trait FeltShrubPlane extends FeltCollectorPlane[FeltShrubBuilder, FeltShrubCollector]

/**
 * Felt Version of Fabric Plane
 */
final
class FeltShrubPlaneContext
  extends FeltCollectorPlaneContext[FeltShrubBuilder, FeltShrubCollector]
    with FeltShrubPlane with FeltShrubPlaneMerge with FeltShrubFactory {

  override def grabCollector(builder: FeltShrubBuilder): FeltShrubCollector =
    planeBinding.collectors.shrubs.grabCollector(builder)

  override def releaseCollector(collector: FeltShrubCollector): Unit =
    planeBinding.collectors.shrubs.releaseCollector(collector)

  override def newBuilder(): FeltShrubBuilder = planeBinding.collectors.shrubs.newBuilder

  override def rowLimitExceeded: Boolean = false

  override def rowCount: Int = 0

  override def planeDictionary: BrioMutableDictionary = null

  override def planeDictionary_=(d: BrioMutableDictionary): Unit = {
  }

  override def dictionaryOverflow: Boolean = false
}
