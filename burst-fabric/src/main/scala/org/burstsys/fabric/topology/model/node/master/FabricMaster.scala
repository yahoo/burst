/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node.master

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.topology.model.node.{FabricNodeContext, FabricNodeId}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort}
import org.burstsys.vitals.properties.VitalsPropertyMap

/**
 * Represents the ''master'' node role in the fabric topology
 *
 */
trait FabricMaster extends FabricMasterNode {

  /**
   * The properties for this master.
   * This is where standard property configuration is done for the master
   *
   * @return
   */
  def masterProperties: VitalsPropertyMap

}

object FabricMaster {

  def apply(
             masterId: FabricNodeId,
             masterNodeName: VitalsHostName,
             masterNodeAddress: VitalsHostAddress,
             masterPort: VitalsHostPort,
             masterProperties: VitalsPropertyMap = Map.empty
           ): FabricMaster =
    FabricMasterContext(
      id = masterId,
      masterNodeName = masterNodeName,
      masterNodeAddress = masterNodeAddress,
      masterPort = masterPort,
      masterProperties = masterProperties
    )

}

private[fabric] final
case class FabricMasterContext(
                                id: Long,
                                masterNodeName: VitalsHostName,
                                masterNodeAddress: VitalsHostAddress,
                                var masterPort: VitalsHostPort,
                                var masterProperties: VitalsPropertyMap
                              )
  extends FabricNodeContext(nodeId = id, nodeName = masterNodeName, nodeAddress = masterNodeAddress) with FabricMaster {

  override
  def toString: String = s"master(${super.toString}, masterPort=$masterPort)"

  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeInt(masterPort)
    // TODO ADD masterProperties
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    masterPort = input.readInt
    // TODO ADD masterProperties
  }
}
