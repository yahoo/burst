/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client

import org.burstsys.gen.thrift.api.client.BTBurstService
import org.burstsys.gen.thrift.api.client.domain.BTDomain
import org.burstsys.gen.thrift.api.client.domain.BTDomainResponse
import org.burstsys.gen.thrift.api.client.query.BTParameter
import org.burstsys.gen.thrift.api.client.query.BTQueryResponse
import org.burstsys.gen.thrift.api.client.view.BTViewResponse
import org.burstsys.gen.thrift.api.client.view.BTView
import org.burstsys.vitals.errors.safely
import org.apache.thrift.async.AsyncMethodCallback

import java.util.{List => JList}

class StubBurstService extends BTBurstService.Iface with BTBurstService.AsyncIface {
  /**
   * ensureDomain performs an upsert to the domain as it may exist in the catalog.
   * Any field in the spec that has a value is assumed to be an update, and any field
   * sent with a `zero value` is merged with the existing catalog value.
   *
   * @param domain the domain that should exist in the catalog. It must specify a udk
   */
  override def ensureDomain(domain: BTDomain): BTDomainResponse = ???

  /**
   * findDomain returns the domain from the catalog, if it exists.
   *
   * @param udk the udk of the domain to fetch from the catalog.
   */
  override def findDomain(udk: String): BTDomainResponse = ???

  /**
   * ensureDomainContainsView performs an upsert to the view in the specified domain, as it may exist in the catalog.
   * ensureDomainContainsView follows the same rules as ensureDomain to determine which fields are updated and
   * which fields are merged.
   *
   * @param domainUdk the udk specifying domain in which the view should exist.
   * @param spec      the view that should exist.
   */
  override def ensureDomainContainsView(domainUdk: String, spec: BTView): BTViewResponse = ???

  /**
   * listViewsInDomain returns any views defined in the specified domain.
   *
   * @param domainUdk the udk of the domain containing the views to be returned.
   */
  override def listViewsInDomain(domainUdk: String): BTViewResponse = ???

  /**
   * executeQuery runs a query
   *
   * @param guid      a unique id for the query. If present it must be of the form [a-zA-Z][a-zA-Z0-9_]{31}
   * @param domainUdk the domain to run the query over
   * @param viewUdk   the view to run the query over
   * @param source    the query text
   * @param timezone  the timezone to use to interpret date times
   * @param params    any parameter values to pass to the query
   */
  override def executeQuery(guid: String, domainUdk: String, viewUdk: String, source: String, timezone: String, params: JList[BTParameter]): BTQueryResponse = ???

  override def ensureDomain(domain: BTDomain, resultHandler: AsyncMethodCallback[BTDomainResponse]): Unit = {
    try {
      resultHandler.onComplete(ensureDomain(domain))
    } catch safely {
      case e: Exception => resultHandler.onError(e)
    }
  }

  override def findDomain(udk: String, resultHandler: AsyncMethodCallback[BTDomainResponse]): Unit = {
    try {
      resultHandler.onComplete(findDomain(udk))
    } catch safely {
      case e: Exception => resultHandler.onError(e)
    }
  }

  override def ensureDomainContainsView(domainUdk: String, spec: BTView, resultHandler: AsyncMethodCallback[BTViewResponse]): Unit = {
    try {
      resultHandler.onComplete(ensureDomainContainsView(domainUdk, spec))
    } catch safely {
      case e: Exception => resultHandler.onError(e)
    }
  }

  override def listViewsInDomain(domainUdk: String, resultHandler: AsyncMethodCallback[BTViewResponse]): Unit = {
    try {
      resultHandler.onComplete(listViewsInDomain(domainUdk))
    } catch safely {
      case e: Exception => resultHandler.onError(e)
    }
  }

  override def executeQuery(guid: String, domainUdk: String, viewUdk: String, source: String, timezone: String, params: JList[BTParameter], resultHandler: AsyncMethodCallback[BTQueryResponse]): Unit = {
    try {
      resultHandler.onComplete(executeQuery(guid, domainUdk, viewUdk, source, timezone, params))
    } catch safely {
      case e: Exception => resultHandler.onError(e)
    }
  }
}
