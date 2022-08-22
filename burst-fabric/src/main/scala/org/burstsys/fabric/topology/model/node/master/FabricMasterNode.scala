/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node.master

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.topology.model.node.{FabricNode, FabricNodeContext, FabricNodeId}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort, convertHostAddressToHostname}

/**
 * Master key in FabricTopology
 */
trait FabricMasterNode extends FabricNode with Equals {

  /**
   * port that connects master <-> worker
   *
   * for masters this will be the place for workers to connect, for workers
   * this will be the port they ha
   *
   * @return
   */
  def masterPort: VitalsHostPort

  /**
   * turn this into a simple serializable universal master key
   *
   * @return
   */
  final
  def forExport: FabricMasterNode = FabricMasterNode(this)

}

object FabricMasterNode {

  def apply(masterId: FabricNodeId, masterNodeAddress: VitalsHostAddress, masterPort: VitalsHostPort): FabricMasterNode =
    FabricMasterNodeContext(
      masterNodeId = masterId, masterNodeName = convertHostAddressToHostname(masterNodeAddress),
      masterNodeAddress = masterNodeAddress, masterPort = masterPort
    )

  def apply(masterId: FabricNodeId, masterNodeName: VitalsHostName, masterNodeAddress: VitalsHostAddress, masterNodePort: VitalsHostPort): FabricMasterNode =
    FabricMasterNodeContext(
      masterNodeId = masterId, masterNodeName = masterNodeName,
      masterNodeAddress = masterNodeAddress, masterPort = masterNodePort
    )

  /**
   * constructor to take any type of master key and turn it into a simple serializable universal one
   *
   * @param master
   * @return
   */
  def apply(master: FabricMasterNode): FabricMasterNode = FabricMasterNodeContext(
    masterNodeId = master.nodeId, masterNodeName = master.nodeName,
    masterNodeAddress = master.nodeAddress, masterPort = master.masterPort
  )
}

private[fabric] final case
class FabricMasterNodeContext(masterNodeId: FabricNodeId, masterNodeName: VitalsHostName, masterNodeAddress: VitalsHostAddress, var masterPort: VitalsHostPort)
  extends FabricNodeContext(nodeId = masterNodeId, nodeName = masterNodeName, nodeAddress = masterNodeAddress) with FabricMasterNode {

  override
  def toString: String = s"master(${super.toString})"

  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeInt(masterPort)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    masterPort = input.readInt
  }
}
