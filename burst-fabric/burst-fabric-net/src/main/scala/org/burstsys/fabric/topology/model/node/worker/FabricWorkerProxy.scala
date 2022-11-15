/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.node.worker

import org.burstsys.fabric.configuration._
import org.burstsys.fabric.container.model.metrics.FabricAssessment
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.topology.model.node.FabricNodeId
import org.burstsys.vitals
import org.burstsys.vitals.json.VitalsJsonRepresentable
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort, convertHostAddressToHostname}

import scala.language.implicitConversions

/**
 * the metadata associated with a worker in the fabric topology
 */
trait FabricWorkerProxy extends FabricWorkerROProxy {

  def connection: FabricNetServerConnection

  /**
   * the worker linux process id
   */
  def workerProcessId_=(p: Int): Unit

  /**
   * the last time we received a heartbeat from the worker
   */
  def lastUpdateTime_=(ts: Long): Unit

  /**
   * Periodic assessment stats collected from the worker
   */
  def assessment_=(a: FabricAssessment): Unit

  /**
   * the time it took to return the last assessment
   */
  def assessLatencyNanos_=(t: Long): Unit

  /**
   * the time between worker epoch send and supervisor epoch recv times.
   * Note this includes both send time and any clock skew
   */
  def tetherSkewMs_=(t: Long): Unit
}

trait FabricWorkerROProxy extends FabricWorkerNode with VitalsJsonRepresentable[JsonFabricWorker] {

  /**
   * this is a connection link string to the client
   */
  def link: String

  /**
   * is this connection connected to the client
   */
  def isConnected: Boolean

  /**
   * the fabric net IP port for the connection on the worker
   */
  def fabricPort: VitalsHostPort

  /**
   * the git commit for the burst jars running on the worker
   */
  def commitId: String

  /**
   * the worker linux process id
   */
  def workerProcessId: Int

  /**
   * the epoch timestamp for when this worker connected
   */
  def connectionTime: Long

  /**
   * the last time we received a heartbeat from the worker
   */
  def lastUpdateTime: Long

  /**
   * Periodic assessment stats collected from the worker
   */
  def assessment: FabricAssessment

  /**
   * the time it took to return the last assessment
   */
  def assessLatencyNanos: Long

  /**
   * the time between worker epoch send and supervisor epoch recv times.
   * Note this includes both send time and any clock skew
   */
  def tetherSkewMs: Long

  def mismatched: Boolean = commitId != vitals.git.commitId

  def state: FabricWorkerState = {
    if (isConnected) {
      val homogeneous = burstFabricTopologyHomogeneous.get
      if (homogeneous && mismatched) FabricWorkerStateExiled else FabricWorkerStateLive
    }
    else FabricWorkerStateDead
  }

}

object FabricWorkerProxy {

  def apply(connection: FabricNetServerConnection, gitCommit: String): FabricWorkerProxy =
    FabricWorkerProxyContext(connection, gitCommit)

}

private[fabric] final case
class FabricWorkerProxyContext(connection: FabricNetServerConnection, commitId: String)
  extends FabricWorkerProxy {

  override def toString: String = s"FabricWorkerProxy(nodeName=$nodeName, nodeAddress=$nodeAddress, supervisorPort=$fabricPort)"

  override def toJson: JsonFabricWorker = JsonFabricWorker(
    assessLatencyNanos, assessment, commitId, connectionTime, lastUpdateTime, fabricPort, mismatched,
    nodeAddress, nodeId, nodeMoniker, nodeName, state, tetherSkewMs, workerProcessId
  )

  ////////////////////////////////////////////////////////////////////////////////////
  // STATE
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _connectionTime: Long = System.currentTimeMillis()

  private[this]
  var _lastUpdateTime: Long = _

  private[this]
  var _processId: Int = _

  private[this]
  var _assessLatencyNs: Long = _

  private[this]
  var _tetherSkewMs: Long = _

  private[this]
  var _assessment: FabricAssessment = _

  ////////////////////////////////////////////////////////////////////////////////////
  // HOST INFO
  // TODO make these all  a FabricHostKey ??
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def connectionTime: Long = _connectionTime

  override
  def lastUpdateTime: Long = _lastUpdateTime

  override
  def lastUpdateTime_=(ts: Long): Unit = _lastUpdateTime = ts

  override
  def nodeId: FabricNodeId = connection.clientKey.nodeId

  override
  lazy val nodeName: VitalsHostName = convertHostAddressToHostname(nodeAddress)

  override
  lazy val nodeMoniker: VitalsHostName = nodeName

  override
  def nodeAddress: VitalsHostAddress = connection.remoteAddress

  override
  def fabricPort: VitalsHostPort = connection.remotePort

  override
  def workerProcessId: VitalsHostPort = _processId

  override
  def workerProcessId_=(p: VitalsHostPort): Unit = _processId = p

  override
  def assessLatencyNanos: Long = _assessLatencyNs

  override
  def assessLatencyNanos_=(t: Long): Unit = _assessLatencyNs = t

  override
  def tetherSkewMs: Long = _tetherSkewMs

  override
  def tetherSkewMs_=(t: Long): Unit = _tetherSkewMs = t

  override
  def assessment: FabricAssessment = _assessment

  override
  def assessment_=(a: FabricAssessment): Unit = _assessment = a

  override def link: VitalsHostAddress = {
    connection.link
  }

  override def isConnected: Boolean = {
    connection.isConnected
  }
}
