/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.thrift

import org.burstsys.gen.thrift.api.client.BTResultStatus
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

trait ThriftRequest {
  def ruid: String

  def timestamp: Long

  def method: String

  def domainUdk: String

  def status: BTResultStatus

  def status_=(s: BTResultStatus): Unit

  def exception: Throwable

  def exception_=(t: Throwable): Unit
}

trait ThriftRequestsListener {
  def beginReqest(ruid: String): Unit

  def calledEnsureDomain(req: EnsureDomainRequest): Unit

  def finishedEnsureDomain(req: EnsureDomainRequest): Unit

  def calledFindDomain(req: FindDomainRequest): Unit

  def finishedFindDomain(req: FindDomainRequest): Unit

  def calledEnsureDomainContainsView(req: EnsureViewRequst): Unit

  def finishedEnsureDomainContainsView(req: EnsureViewRequst): Unit

  def calledListViewsInDomain(req: ListViewsRequest): Unit

  def finishedListViewsInDomain(req: ListViewsRequest): Unit

  def calledExecuteQuery(req: ExecuteQueryRequest): Unit

  def finishedExecuteQuery(req: ExecuteQueryRequest): Unit

  def requestEncounteredException(ruid: String, ex: Throwable): Unit
}


final case class ThriftRequestLog() {

  private val requests = new ConcurrentHashMap[String, ThriftRequest]()

  private val orderedRequests = new ConcurrentLinkedQueue[String]()

  private val requestsToProcess = new ConcurrentLinkedQueue[String]()

  private val listeners = ArrayBuffer[ThriftRequestsListener]()

  private val requestsTender = new VitalsBackgroundFunction("thrift-request-log-tender", 1 minute, 1 minute, {
    var requestsToDrop = Math.max(0, requests.size - 2000)
    while (requestsToDrop > 0) {
      requestsToDrop -= 1
      val ruid = orderedRequests.poll()
      requests.remove(ruid)
    }
  })
  requestsTender.start

  private def nextReqGuid: String = requestsToProcess.poll()

  private def timestamp(ruid: String): Long = requests.get(ruid) match {
    case null => -1
    case req => req.timestamp
  }

  def talksTo(listener: ThriftRequestsListener): Unit = listeners += listener

  def allRequests: Array[ThriftRequest] = orderedRequests.asScala.map(requests.get).toArray

  def beginReqest(ruid: String): Unit = {
    orderedRequests.add(ruid)
    requestsToProcess.add(ruid)
    requests.put(ruid, UnknownRequest(ruid))
    notifyListeners(_.beginReqest(ruid))
  }

  def calledEnsureDomain(req: EnsureDomainRequest): String = {
    val ruid = nextReqGuid
    val request = req.copy(ruid = ruid, timestamp = timestamp(ruid))
    requests.put(ruid, request)
    notifyListeners(_.calledEnsureDomain(request))
    ruid
  }

  def finishedEnsureDomain(req: EnsureDomainRequest): Unit = {
    requests.put(req.ruid, req.copy(timestamp = timestamp(req.ruid)))
    notifyListeners(_.finishedEnsureDomain(req))
  }

  def calledFindDomain(req: FindDomainRequest): String = {
    val ruid = nextReqGuid
    val request = req.copy(ruid = ruid, timestamp = timestamp(ruid))
    requests.put(ruid, request)
    notifyListeners(_.calledFindDomain(request))
    ruid
  }

  def finishedFindDomain(req: FindDomainRequest): Unit = {
    requests.put(req.ruid, req.copy(timestamp = timestamp(req.ruid)))
    notifyListeners(_.finishedFindDomain(req))
  }

  def calledEnsureDomainContainsView(req: EnsureViewRequst): String = {
    val ruid = nextReqGuid
    val request = req.copy(ruid = ruid, timestamp = timestamp(ruid))
    requests.put(ruid, request)
    notifyListeners(_.calledEnsureDomainContainsView(request))
    ruid
  }

  def finishedEnsureDomainContainsView(req: EnsureViewRequst): Unit = {
    requests.put(req.ruid, req.copy(timestamp = timestamp(req.ruid)))
    notifyListeners(_.finishedEnsureDomainContainsView(req))
  }

  def calledListViewsInDomain(req: ListViewsRequest): String = {
    val ruid = nextReqGuid
    val request = req.copy(ruid = ruid, timestamp = timestamp(ruid))
    requests.put(ruid, request)
    notifyListeners(_.calledListViewsInDomain(request))
    ruid
  }

  def finishedListViewsInDomain(req: ListViewsRequest): Unit = {
    requests.put(req.ruid, req.copy(timestamp = timestamp(req.ruid)))
    notifyListeners(_.finishedListViewsInDomain(req))
  }

  def calledExecuteQuery(req: ExecuteQueryRequest): String = {
    val ruid = nextReqGuid
    val request = req.copy(ruid = ruid, timestamp = timestamp(ruid))
    requests.put(ruid, request)
    notifyListeners(_.calledExecuteQuery(request))
    ruid
  }

  def finishedExecuteQuery(req: ExecuteQueryRequest): Unit = {
    requests.put(req.ruid, req)
    notifyListeners(_.finishedExecuteQuery(req))
  }

  def requestEncounteredException(ruid: String, ex: Throwable): Unit = {
    if (requestsToProcess.remove(ruid)) {
      // we failed before we could figure out what's going on
      requests.put(ruid, UnknownRequest(ruid, exception = ex))
    } else {
      val request = requests.get(ruid)
      request.status = BTResultStatus.ExceptionStatus
      request.exception = ex
    }
    notifyListeners(_.requestEncounteredException(ruid, ex))
  }

  private def notifyListeners(event: ThriftRequestsListener => Unit): Unit = {
    TeslaRequestFuture(
      listeners.foreach(event)
    )
  }
}

////////////////////////////////////////////////
// Message Definitions
////////////////////////////////////////////////

final case class EnsureDomainRequest(
                                      ruid: String = "",
                                      domainUdk: String,
                                      override var status: BTResultStatus = BTResultStatus.InProgressStatus,
                                      override var exception: Throwable = null,
                                      timestamp: Long = System.currentTimeMillis()
                                    ) extends ThriftRequest {
  override val method: String = "EnsureDomain"
}

final case class FindDomainRequest(
                                    ruid: String = "",
                                    domainUdk: String,
                                    override var status: BTResultStatus = BTResultStatus.InProgressStatus,
                                    override var exception: Throwable = null,
                                    timestamp: Long = System.currentTimeMillis()
                                  ) extends ThriftRequest {
  override val method: String = "FindDomain"
}

final case class EnsureViewRequst(
                                   ruid: String = "",
                                   domainUdk: String,
                                   viewUdk: String,
                                   override var status: BTResultStatus = BTResultStatus.InProgressStatus,
                                   override var exception: Throwable = null,
                                   timestamp: Long = System.currentTimeMillis()
                                 ) extends ThriftRequest {
  override val method: String = "EnsureDomainContainsView"
}

final case class ListViewsRequest(
                                   ruid: String = "",
                                   domainUdk: String,
                                   override var status: BTResultStatus = BTResultStatus.InProgressStatus,
                                   override var exception: Throwable = null,
                                   timestamp: Long = System.currentTimeMillis()
                                 ) extends ThriftRequest {
  override val method: String = "ListViewsInDomain"
}

final case class ExecuteQueryRequest(
                                      ruid: String = "",
                                      guid: String,
                                      domainUdk: String,
                                      viewUdk: String,
                                      source: String,
                                      params: Map[String, Any] = Map.empty,
                                      results: Map[String, Any] = Map.empty,
                                      override var status: BTResultStatus = BTResultStatus.InProgressStatus,
                                      override var exception: Throwable = null,
                                      timestamp: Long = System.currentTimeMillis()
                                    ) extends ThriftRequest {
  override val method: String = "ExecuteQuery"
}

final case class UnknownRequest(
                                 ruid: String,
                                 override var status: BTResultStatus = BTResultStatus.ExceptionStatus,
                                 override var exception: Throwable = null,
                                 timestamp: Long = System.currentTimeMillis()
                               ) extends ThriftRequest {
  override val method: String = "???"

  override val domainUdk = "???"
}
