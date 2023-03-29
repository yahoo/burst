/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.websocket

import org.burstsys.fabric.configuration.burstFabricTopologyHomogeneous
import org.burstsys.fabric.container.http.FabricWebSocket
import org.burstsys.fabric.container.http.FabricWebSocketGroup
import org.burstsys.fabric.container.http.FabricWebSocketListener
import org.burstsys.fabric.container.http.FabricWebSocketService
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.model.node.worker.JsonFabricWorker
import org.burstsys.fabric.topology.supervisor.FabricSupervisorTopology
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.supervisor.http.endpoints.ClientWebsocketMessage
import org.burstsys.supervisor.http.websocket.TopologyMessages.FabricTopologyMessage
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.logging.burstStdMsg

import scala.concurrent.duration._
import scala.language.postfixOps

final case class BurstTopologyRelay(topology: FabricSupervisorTopology, websocketService: FabricWebSocketService)
  extends FabricWebSocketListener with FabricTopologyListener {

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Private State
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  private val _socketGroup = websocketService.open("/workers", this)

  private val _topologyBroadcaster = new VitalsBackgroundFunction("supervisor-worker-ws-updater", 1 second, 10 seconds,
    _socketGroup.broadcastJson(FabricTopologyMessage(workersJson))
  )
  _topologyBroadcaster.start

  def workersJson: Array[JsonFabricWorker] = {
    topology.allWorkers
      .map(topology.getWorker(_, mustBeConnected = false))
      .collect({ case w if w.isDefined => w.get.toJson })
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // web socket listener
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def onWebSocketOpen(group: FabricWebSocketGroup, socket: FabricWebSocket): Unit = {
    log trace burstStdMsg("web socket open")
    socket.sendJson(FabricTopologyMessage(workersJson))
  }

  override def onWebSocketAction(group: FabricWebSocketGroup, socket: FabricWebSocket, action: String, payload: Map[String, Any]): Unit = {
    action match {
      case "set-homogeneity" =>
        payload.get("required") match {
          case Some(required: Boolean) =>
            burstFabricTopologyHomogeneous.set(required)
            group.broadcastJson(FabricTopologyMessage(workersJson))
          case other =>
            log info s"FabricTopologyRelay - nonboolean provided to toggle-homogeneity $other"
        }
      case _ =>
        log info s"FabricTopologyRelay - unknown action '$action'"
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // web socket listener
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * called to let listeners know that other listeners now know about the worker
   */
  override def onTopologyWorkerGained(worker: FabricTopologyWorker): Unit =
    _socketGroup.broadcastJson(FabricTopologyMessage(workersJson))

  /**
   * called to let listeners know that other listeners know the worker was lost
   */
  override def onTopologyWorkerLost(worker: FabricTopologyWorker): Unit =
    _socketGroup.broadcastJson(FabricTopologyMessage(workersJson))

  /**
   * a significant change has happened to a worker other than gain or loss
   */
  override def onTopologyWorkerChange(worker: FabricTopologyWorker): Unit =
    _socketGroup.broadcastJson(FabricTopologyMessage(workersJson))
}

object TopologyMessages {

  object FabricTopologyMessage {
    def apply(workers: Array[JsonFabricWorker]): ClientWebsocketMessage =
      FabricWorkersMessageContext(workers = workers, homogeneous = burstFabricTopologyHomogeneous.get)
  }

  private final
  case class FabricWorkersMessageContext(workers: Array[JsonFabricWorker], homogeneous: Boolean) extends ClientWebsocketMessage {
    val msgType = "update"
  }
}
