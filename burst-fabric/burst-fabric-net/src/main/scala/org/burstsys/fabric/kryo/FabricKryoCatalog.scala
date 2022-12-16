/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.kryo

import org.burstsys.fabric.container.metrics.FabricAssessment
import org.burstsys.fabric.container.model.metrics.FabricLastHourMetric
import org.burstsys.fabric.container.model.metrics.FabricMetricTuple
import org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisorContext
import org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisorNodeContext
import org.burstsys.fabric.topology.model.node.worker._
import org.burstsys.vitals.kryo._

import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.unused

/**
 * Kryo Serialized Class Register
 */
@unused
class FabricKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(fabricCatalogStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key.synchronized {
      Array(
        /////////////////////////////////////////////////////////////////////////////////
        // assessment
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricAssessment]),
        (key.getAndIncrement, classOf[FabricMetricTuple]),
        (key.getAndIncrement, classOf[Array[FabricMetricTuple]]),
        (key.getAndIncrement, classOf[FabricLastHourMetric]),

        /////////////////////////////////////////////////////////////////////////////////
        // topology
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricSupervisorContext]),
        (key.getAndIncrement, classOf[FabricWorkerNodeContext]),
        (key.getAndIncrement, classOf[FabricSupervisorNodeContext]),

        /////////////////////////////////////////////////////////////////////////////////
        // worker state
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, FabricWorkerStateUnknown.getClass),
        (key.getAndIncrement, FabricWorkerStateLive.getClass),
        (key.getAndIncrement, FabricWorkerStateTardy.getClass),
        (key.getAndIncrement, FabricWorkerStateFlaky.getClass),
        (key.getAndIncrement, FabricWorkerStateDead.getClass),
        (key.getAndIncrement, FabricWorkerStateExiled.getClass),

      )
    }
}
