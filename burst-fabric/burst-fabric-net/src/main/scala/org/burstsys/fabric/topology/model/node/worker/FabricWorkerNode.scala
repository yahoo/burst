/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node.worker

import org.burstsys.fabric.topology.model.node.{FabricNode, FabricNodeContext, FabricNodeId, UnknownFabricNodeId}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, convertHostAddressToHostname}

/**
 * Worker key in FabricTopology
 */
trait FabricWorkerNode extends FabricNode with Equals {

  /**
   * turn this into a simple serializable universal worker key
   *
   * @return
   */
  final
  def forExport: FabricWorkerNode = FabricWorkerNode(this)
}

object FabricWorkerNode {

  def apply(workerId: FabricNodeId, workerNodeAddress: VitalsHostAddress): FabricWorkerNode =
    FabricWorkerNodeContext(
      workerId = workerId, workerNodeName = convertHostAddressToHostname(workerNodeAddress),
      workerNodeAddress = workerNodeAddress
    )

  /**
   * constructor to take any type of worker key and turn it into a simple serializable universal one
   */
  def apply(worker: FabricWorkerNode): FabricWorkerNode = FabricWorkerNodeContext(
    workerId = worker.nodeId, workerNodeName = worker.nodeName,
    workerNodeAddress = worker.nodeAddress
  )
}

private[fabric] case
class FabricWorkerNodeContext(workerId: FabricNodeId, workerNodeName: VitalsHostName, workerNodeAddress: VitalsHostAddress)
  extends FabricNodeContext(nodeId = workerId, nodeName = workerNodeName, nodeAddress = workerNodeAddress) with FabricWorkerNode {

  def this() = this(UnknownFabricNodeId, null, null)

  override
  def toString: String = s"worker(${super.toString})"

}
