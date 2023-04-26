/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node.worker

import org.burstsys.fabric.topology.model.node.{FabricNode, FabricNodeContext, FabricNodeId, UnknownFabricNodeId}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, convertHostAddressToHostname}

/**
 * Worker key in FabricTopology
 */
trait FabricWorkerNode extends FabricNode with Equals {

  /**
   * @return turn this into a simple serializable universal worker key
   */
  final def forExport: FabricWorkerNode = FabricWorkerNode(this)
}

object FabricWorkerNode {

  def apply(workerId: FabricNodeId, workerNodeAddress: VitalsHostAddress): FabricWorkerNode =
    new FabricWorkerNodeContext(workerId, convertHostAddressToHostname(workerNodeAddress), workerNodeAddress, "")

  /**
   * constructor to take any type of worker key and turn it into a simple serializable universal one
   */
  def apply(worker: FabricWorkerNode): FabricWorkerNode =
    new FabricWorkerNodeContext(worker.nodeId, worker.nodeName, worker.nodeAddress, worker.nodeMoniker)
}

private[fabric] class FabricWorkerNodeContext(
                                               workerId: FabricNodeId,
                                               workerNodeName: VitalsHostName,
                                               workerNodeAddress: VitalsHostAddress,
                                               override val nodeMoniker: VitalsHostName
                                             )
  extends FabricNodeContext(workerId, workerNodeName, workerNodeAddress) with FabricWorkerNode {

  def this() = this(UnknownFabricNodeId, null, null, null)

  override
  def toString: String = s"worker(${super.toString})"

}
