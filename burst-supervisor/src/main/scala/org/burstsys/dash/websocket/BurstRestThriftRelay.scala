/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.websocket

import org.burstsys.dash.application.websocket.BurstDashWebSocketListener
import org.burstsys.dash.application.websocket.BurstWebSocket
import org.burstsys.dash.application.websocket.BurstWebSocketGroup
import org.burstsys.dash.application.websocket.BurstWebSocketService
import org.burstsys.dash.service.thrift.EnsureDomainRequest
import org.burstsys.dash.service.thrift.EnsureViewRequst
import org.burstsys.dash.service.thrift.ExecuteQueryRequest
import org.burstsys.dash.service.thrift.FindDomainRequest
import org.burstsys.dash.service.thrift.ListViewsRequest
import org.burstsys.dash.service.thrift.ThriftRequestsListener
import org.burstsys.dash.service.thrift.requestLog
import org.burstsys.dash.websocket.ThriftRequests.AllThriftRequests
import org.burstsys.dash.websocket.ThriftRequests.ThriftRequestEncouteredException
import org.burstsys.dash.websocket.ThriftRequests.ThriftRequestReceived
import org.burstsys.dash.websocket.ThriftRequests.ThriftRequestUpdate

case class BurstRestThriftRelay(webSocketService: BurstWebSocketService) extends BurstDashWebSocketListener with ThriftRequestsListener {

  private val _socketGroup = webSocketService.open("/thrift", this)

  override def onWebSocketOpen(group: BurstWebSocketGroup, socket: BurstWebSocket): Unit = {
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
