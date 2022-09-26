/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node.supervisor

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.topology.model.node.{FabricNodeContext, FabricNodeId}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort}
import org.burstsys.vitals.properties.VitalsPropertyMap

/**
 * Represents the ''supervisor'' node role in the fabric topology
 *
 */
trait FabricSupervisor extends FabricSupervisorNode {

  /**
   * The properties for this supervisor.
   * This is where standard property configuration is done for the supervisor
   *
   * @return
   */
  def supervisorProperties: VitalsPropertyMap

}

object FabricSupervisor {

  def apply(supervisorId: FabricNodeId, supervisorNodeName: VitalsHostName, supervisorNodeAddress: VitalsHostAddress, supervisorPort: VitalsHostPort, supervisorProperties: VitalsPropertyMap = Map.empty): FabricSupervisor =
    FabricSupervisorContext(
      id = supervisorId,
      supervisorNodeName = supervisorNodeName,
      supervisorNodeAddress = supervisorNodeAddress,
      supervisorPort = supervisorPort,
      supervisorProperties = supervisorProperties
    )

}

private[fabric] final
case class FabricSupervisorContext(id: FabricNodeId, supervisorNodeName: VitalsHostName, supervisorNodeAddress: VitalsHostAddress, var supervisorPort: VitalsHostPort, var supervisorProperties: VitalsPropertyMap)
  extends FabricNodeContext(nodeId = id, nodeName = supervisorNodeName, nodeAddress = supervisorNodeAddress) with FabricSupervisor {

  override
  def toString: String = s"supervisor(${super.toString}, supervisorPort=$supervisorPort)"

  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeInt(supervisorPort)
    // TODO ADD supervisorProperties
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    supervisorPort = input.readInt
    // TODO ADD supervisorProperties
  }
}
