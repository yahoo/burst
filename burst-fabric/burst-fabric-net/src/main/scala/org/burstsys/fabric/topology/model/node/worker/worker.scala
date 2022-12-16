/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node

import org.burstsys.fabric.container.metrics.FabricAssessment
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostAddress
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.net.VitalsHostPort

package object worker extends VitalsLogger {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Worker State
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  sealed case class FabricWorkerState(code: Int) {
    override def toString: String =
      s"${getClass.getSimpleName.stripPrefix("FabricWorkerState").stripSuffix("$")}".toUpperCase
  }

  /**
   * Worker should never be in this state...
   */
  object FabricWorkerStateUnknown extends FabricWorkerState(0)

  /**
   * Normal operating state for a worker
   */
  object FabricWorkerStateLive extends FabricWorkerState(1)

  /**
   * Worker has missed at least one heartbeat timeout
   */
  object FabricWorkerStateTardy extends FabricWorkerState(2)

  /**
   * Worker is repeatedly getting into trouble.
   */
  object FabricWorkerStateFlaky extends FabricWorkerState(3)

  /**
   * Worker has been missing for a while and does not seem to be coming back
   */
  object FabricWorkerStateDead extends FabricWorkerState(4)

  /**
   * Worker has been removed from active list to protect system
   */
  object FabricWorkerStateExiled extends FabricWorkerState(5)

  final case class FabricWorkerParameters(supervisorAddress: VitalsHostName = "", supervisorPort: VitalsHostPort = 0)

  /**
   * used for Json worker representation
   *
   */
  final case
  class JsonFabricWorker(
                          assessLatencyNanos: Long,
                          assessment: FabricAssessment,
                          commitId: String,
                          connectionTime: Long,
                          lastUpdateTime: Long,
                          fabricPort: VitalsHostPort,
                          mismatched: Boolean,
                          nodeAddress: VitalsHostAddress,
                          nodeId: FabricNodeId,
                          nodeMoniker: String,
                          nodeName: VitalsHostName,
                          state: FabricWorkerState,
                          tetherSkewMs: Long,
                          workerProcessId: Int,
                        ) extends VitalsJsonObject

}
