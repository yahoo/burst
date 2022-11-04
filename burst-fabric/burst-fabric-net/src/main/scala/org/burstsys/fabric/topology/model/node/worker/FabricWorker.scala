/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node.worker

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.topology.model.node.{FabricNodeContext, FabricNodeId}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, convertHostnameToAddress}
import org.burstsys.vitals.properties.VitalsPropertyMap

/**
 * Represents the ''worker'' node role in the fabric topology
 *
 */
trait FabricWorker extends FabricWorkerNode {

  /**
   * The properties for this worker.
   * This is where standard property configuration is done for the worker
   *
   * @return
   */
  def workerProperties: VitalsPropertyMap
}

object FabricWorker {

  def apply(
             id: Long,
             moniker: String,
             nodeName: VitalsHostName,
             nodeAddress: VitalsHostAddress,
             properties: VitalsPropertyMap = Map.empty
           ): FabricWorker =
    FabricWorkerContext(id, moniker, nodeName, nodeAddress, properties)
}

private[fabric] final case
class FabricWorkerContext(
                           workerId: FabricNodeId,
                           workerMoniker: String,
                           workerNodeName: VitalsHostName,
                           workerNodeAddress: VitalsHostAddress,
                           workerProperties: VitalsPropertyMap
                         )
  extends FabricNodeContext(workerId, workerNodeName, workerNodeAddress) with FabricWorker {

  override def toString: String = s"worker(${super.toString})"

  override def nodeMoniker: String = workerMoniker

  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    // TODO add workerProperties ??
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    // TODO add workerProperties ??
  }

}
