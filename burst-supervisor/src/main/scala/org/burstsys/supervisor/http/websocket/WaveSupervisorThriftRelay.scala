/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.websocket

import org.burstsys.fabric.container.http.FabricWebSocket
import org.burstsys.fabric.container.http.FabricWebSocketGroup
import org.burstsys.fabric.container.http.FabricWebSocketListener
import org.burstsys.fabric.container.http.FabricWebSocketService
import org.burstsys.supervisor.http.endpoints.ClientWebsocketMessage
import org.burstsys.supervisor.http.service.thrift.EnsureDomainRequest
import org.burstsys.supervisor.http.service.thrift.EnsureViewRequst
import org.burstsys.supervisor.http.service.thrift.ExecuteQueryRequest
import org.burstsys.supervisor.http.service.thrift.FindDomainRequest
import org.burstsys.supervisor.http.service.thrift.ListViewsRequest
import org.burstsys.supervisor.http.service.thrift.ThriftRequest
import org.burstsys.supervisor.http.service.thrift.ThriftRequestsListener
import org.burstsys.supervisor.http.service.thrift.requestLog
import org.burstsys.supervisor.http.websocket.ThriftMessages.AllThriftRequests
import org.burstsys.supervisor.http.websocket.ThriftMessages.ThriftRequestEncouteredException
import org.burstsys.supervisor.http.websocket.ThriftMessages.ThriftRequestReceived
import org.burstsys.supervisor.http.websocket.ThriftMessages.ThriftRequestUpdate
import org.burstsys.vitals.uid.VitalsUid

case class BurstThriftRelay(webSocketService: FabricWebSocketService) extends FabricWebSocketListener with ThriftRequestsListener {

  private val _socketGroup = webSocketService.open("/thrift", this)

  override def onWebSocketOpen(group: FabricWebSocketGroup, socket: FabricWebSocket): Unit = {
    socket.sendJson(AllThriftRequests(requestLog.allRequests))
  }

  override def beginReqest(ruid: String): Unit = {
    _socketGroup.broadcastJson(ThriftRequestReceived(ruid))
  }

  override def calledEnsureDomain(req: EnsureDomainRequest): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }
  override def finishedEnsureDomain(req: EnsureDomainRequest): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }

  override def calledFindDomain(req: FindDomainRequest): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }
  override def finishedFindDomain(req: FindDomainRequest): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }

  override def calledEnsureDomainContainsView(req: EnsureViewRequst): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }
  override def finishedEnsureDomainContainsView(req: EnsureViewRequst): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }

  override def calledListViewsInDomain(req: ListViewsRequest): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }
  override def finishedListViewsInDomain(req: ListViewsRequest): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }

  override def calledExecuteQuery(req: ExecuteQueryRequest): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }
  override def finishedExecuteQuery(req: ExecuteQueryRequest): Unit = {
    _socketGroup.broadcastJson(ThriftRequestUpdate(req))
  }

  override def requestEncounteredException(ruid: String, ex: Throwable): Unit = {
    _socketGroup.broadcastJson(ThriftRequestEncouteredException(ruid, ex))
  }
}

object ThriftMessages {
    final case class AllThriftRequests(requests: Array[ThriftRequest]) extends ClientWebsocketMessage {
      val msgType: String = "AllRequests"
    }

    final case class ThriftRequestReceived(ruid: VitalsUid) extends ClientWebsocketMessage {
      val msgType: String = "RequestReceived"
    }
    final case class ThriftRequestUpdate(req: ThriftRequest) extends ClientWebsocketMessage {
      val msgType: String = "RequestUpdate"
    }
    final case class ThriftRequestEncouteredException(ruid: VitalsUid, exception: Throwable) extends ClientWebsocketMessage {
      val msgType: String = "RequestEncouteredException"
    }
}
