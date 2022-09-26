/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName}

/**
 * == Nodes are a JVM container running on a physical or virtual ''host'' ==
 * The Fabric representative for the Burst '''Node''' concept. A Node is a container running in a JVM
 * that is acting as either a supervisor or worker (or both)
 * == Semantics ==
 * <ol>
 * <li>A node can be acting as a '''worker''' or '''supervisor''' or ''both''</li>
 * <li>nodes belong to '''at most one''' cell (a node can be ''not assigned'' to a node)</li>
 * <li>nodes are persisted to the catalog</li>
 * </ol>
 *
 * @see [[org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisor]]
 *      [[org.burstsys.fabric.topology.model.node.worker.FabricWorker]]
 */
trait FabricNode extends Any with Equals with Ordered[FabricNode] {

  /**
   * @return the friendly name for this node
   */
  def nodeMoniker: String = nodeName

  /**
   * @return the host name for this node
   */
  def nodeName: VitalsHostName

  /**
   * @return the IP Address for this node
   */
  def nodeAddress: VitalsHostAddress

  /**
   * this is partially to support unit tests that have more than one worker context in a JVM
   * @return the cell-unique id for this node
   */
  def nodeId: FabricNodeId

  def nodeId_=(id: FabricNodeId): Unit = throw VitalsException(s"inappropriate attempt to assign node id on proxy") // should not happen

  ///////////////////////////////////////////////////////////////////
  // EQUALITY/IDENTITY/ORDERING
  ///////////////////////////////////////////////////////////////////

  final override
  def canEqual(that: Any): Boolean = that.isInstanceOf[FabricNode]

  final override
  def hashCode(): Int = nodeId.hashCode

  final override
  def equals(obj: scala.Any): Boolean = {
    obj match {
      case that: FabricNode =>
        this.nodeId == that.nodeId
      case _ => false
    }
  }

  final override
  def compare(that: FabricNode): Int = nodeId.compareTo(that.nodeId)

}

abstract
class FabricNodeContext(override var nodeId: FabricNodeId, var nodeName: VitalsHostName, var nodeAddress: VitalsHostAddress)
  extends FabricNode with KryoSerializable {

  override
  def toString: String = s"nodeId=$nodeId, nodeName=$nodeName, nodeAddress=$nodeAddress"

  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeString nodeAddress
    output writeString nodeName
    output writeLong nodeId
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    nodeAddress = input.readString
    nodeName = input.readString
    nodeId = input.readLong
  }

}


