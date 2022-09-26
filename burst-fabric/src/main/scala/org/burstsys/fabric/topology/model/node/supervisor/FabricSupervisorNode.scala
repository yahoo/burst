/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node.supervisor

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.topology.model.node.{FabricNode, FabricNodeContext, FabricNodeId}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort, convertHostAddressToHostname}

/**
 * Supervisor key in FabricTopology
 */
trait FabricSupervisorNode extends FabricNode with Equals {

  /**
   * port that connects supervisor <-> worker
   *
   * for supervisors this will be the place for workers to connect, for workers
   * this will be the port they ha
   *
   * @return
   */
  def supervisorPort: VitalsHostPort

  /**
   * turn this into a simple serializable universal supervisor key
   *
   * @return
   */
  final
  def forExport: FabricSupervisorNode = FabricSupervisorNode(this)

}

object FabricSupervisorNode {

  def apply(supervisorId: FabricNodeId, supervisorNodeAddress: VitalsHostAddress, supervisorPort: VitalsHostPort): FabricSupervisorNode =
    FabricSupervisorNodeContext(
      supervisorNodeId = supervisorId, supervisorNodeName = convertHostAddressToHostname(supervisorNodeAddress),
      supervisorNodeAddress = supervisorNodeAddress, supervisorPort = supervisorPort
    )

  def apply(supervisorId: FabricNodeId, supervisorNodeName: VitalsHostName, supervisorNodeAddress: VitalsHostAddress, supervisorNodePort: VitalsHostPort): FabricSupervisorNode =
    FabricSupervisorNodeContext(
      supervisorNodeId = supervisorId, supervisorNodeName = supervisorNodeName,
      supervisorNodeAddress = supervisorNodeAddress, supervisorPort = supervisorNodePort
    )

  /**
   * constructor to take any type of supervisor key and turn it into a simple serializable universal one
   *
   * @param supervisor
   * @return
   */
  def apply(supervisor: FabricSupervisorNode): FabricSupervisorNode = FabricSupervisorNodeContext(
    supervisorNodeId = supervisor.nodeId, supervisorNodeName = supervisor.nodeName,
    supervisorNodeAddress = supervisor.nodeAddress, supervisorPort = supervisor.supervisorPort
  )
}

private[fabric] final case
class FabricSupervisorNodeContext(supervisorNodeId: FabricNodeId, supervisorNodeName: VitalsHostName, supervisorNodeAddress: VitalsHostAddress, var supervisorPort: VitalsHostPort)
  extends FabricNodeContext(nodeId = supervisorNodeId, nodeName = supervisorNodeName, nodeAddress = supervisorNodeAddress) with FabricSupervisorNode {

  override
  def toString: String = s"supervisor(${super.toString})"

  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeInt(supervisorPort)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    supervisorPort = input.readInt
  }
}
