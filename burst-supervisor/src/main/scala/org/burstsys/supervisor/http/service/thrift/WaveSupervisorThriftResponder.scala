/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.thrift

import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogExceptions.CatalogNotFoundException
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.model.view.CatalogView
import org.burstsys.client.util.DatumValue
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.gen.thrift.api.client.BTBurstService
import org.burstsys.gen.thrift.api.client.BTDataFormat
import org.burstsys.gen.thrift.api.client.domain.BTDomain
import org.burstsys.gen.thrift.api.client.domain.BTDomainResponse
import org.burstsys.gen.thrift.api.client.query.BTParameter
import org.burstsys.gen.thrift.api.client.query.BTQueryResponse
import org.burstsys.gen.thrift.api.client.view.BTView
import org.burstsys.gen.thrift.api.client.view.BTViewResponse
import org.burstsys.gen.thrift.api.client.{BTResultStatus => tStatus}
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.safely

import java.util
import scala.concurrent.Await
import scala.concurrent.TimeoutException
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.existentials
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success

case class WaveSupervisorThriftResponder(catalog: CatalogService, agent: AgentService) extends BTBurstService.Iface {

  /**
   * ensureDomain performs an upsert to the domain as it may exist in the catalog.
   * Any field in the spec that has a value is assumed to be an update, and any field
   * sent with a `zero value` is merged with the existing catalog value.
   *
   * @param domain the domain that should exist in the catalog. It must specify a udk
   */
  override def ensureDomain(btDomain: BTDomain): BTDomainResponse = {
    log.info("CLIENT_THRIFT method=ensureDomain")
    val ruid = requestLog.calledEnsureDomain(EnsureDomainRequest(domainUdk = btDomain.udk))
    val domainPk = catalog.ensureDomain(domains.fromThriftDomain(btDomain)) match {
      case Failure(ex) =>
        requestLog.finishedEnsureDomain(EnsureDomainRequest(ruid, btDomain.udk, tStatus.ExceptionStatus, ex))
        return DomainResponse(ex)
      case Success(pk) => pk
    }

    val ensureViewsFailure = views.fromThriftDomain(domainPk, btDomain).foldLeft(null: Throwable)(
      (failure: Throwable, view: CatalogView) =>
        if (failure != null) failure
        else catalog.ensureViewInDomain(btDomain.udk, view) match {
          case Failure(ex) => ex
          case Success(_) => null
        }
    )

    if (ensureViewsFailure == null) {
      getDomain(btDomain.udk,
        { status => requestLog.finishedEnsureDomain(EnsureDomainRequest(ruid, btDomain.udk, status)) },
        { exception => requestLog.finishedEnsureDomain(EnsureDomainRequest(ruid, btDomain.udk, tStatus.ExceptionStatus, exception)) }
      )
    } else {
      requestLog.finishedEnsureDomain(EnsureDomainRequest(ruid, btDomain.udk, tStatus.ExceptionStatus, ensureViewsFailure))
      DomainResponse(ensureViewsFailure)
    }
  }

  /**
   * findDomain returns the domain from the catalog, if it exists.
   *
   * @param udk the udk of the domain to fetch from the catalog.
   */
  override def findDomain(udk: String): BTDomainResponse = {
    log.info("CLIENT_THRIFT method=findDomain")
    val ruid = requestLog.calledFindDomain(FindDomainRequest(domainUdk = udk))
    getDomain(udk,
      { status => requestLog.finishedFindDomain(FindDomainRequest(ruid, udk, status)) },
      { exception => requestLog.finishedFindDomain(FindDomainRequest(ruid, udk, tStatus.ExceptionStatus, exception)) }
    )
  }

  /**
   * ensureDomainContainsView performs an upsert to the view in the specified domain, as it may exist in the catalog.
   * ensureDomainContainsView follows the same rules as ensureDomain to determine which fields are updated and
   * which fields are merged.
   *
   * @param domainUdk the udk specifying domain in which the view should exist.
   * @param btView    the view that should exist.
   */
  override def ensureDomainContainsView(domainUdk: String, btView: BTView): BTViewResponse = {
    log.info("CLIENT_THRIFT method=ensureDomainContainsView")
    val ruid = requestLog.calledEnsureDomainContainsView(EnsureViewRequst(domainUdk = domainUdk, viewUdk = btView.udk))
    val domain = catalog.findDomainByUdk(domainUdk) match {
      case Failure(ex) =>
        requestLog.finishedEnsureDomainContainsView(EnsureViewRequst(ruid, domainUdk, btView.udk, tStatus.ExceptionStatus, ex))
        return ViewResponse(ex)
      case Success(domain) => domain
    }

    val view = views.fromThriftView(domain.pk, btView)
    catalog.ensureViewInDomain(domainUdk, view) match {
      case Failure(ex) =>
        requestLog.finishedEnsureDomainContainsView(EnsureViewRequst(ruid, domainUdk, btView.udk, tStatus.ExceptionStatus, ex))
        ViewResponse(ex)
      case Success(view) =>
        requestLog.finishedEnsureDomainContainsView(EnsureViewRequst(ruid, domainUdk, btView.udk, tStatus.SuccessStatus))
        ViewResponse(domainUdk, view)
    }
  }

  /**
   * listViewsInDomain returns any views defined in the specified domain.
   *
   * @param domainUdk the udk of the domain containing the views to be returned.
   */
  override def listViewsInDomain(domainUdk: String): BTViewResponse = {
    log.info("CLIENT_THRIFT method=listViewsInDomain")
    val ruid = requestLog.calledListViewsInDomain(ListViewsRequest(domainUdk = domainUdk))
    catalog.findDomainWithViewsByUdk(domainUdk) match {
      case Failure(exception) =>
        requestLog.finishedListViewsInDomain(ListViewsRequest(ruid, domainUdk, tStatus.ExceptionStatus, exception))
        ViewResponse(exception)
      case Success((_, views)) =>
        requestLog.finishedListViewsInDomain(ListViewsRequest(ruid, domainUdk, tStatus.SuccessStatus))
        ViewResponse(domainUdk, views.toArray)
    }
  }

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
  override def executeQuery(guid: String, domainUdk: String, viewUdk: String, source: String, timezone: String, params: util.List[BTParameter]): BTQueryResponse = {
    log.info(s"CLIENT_THRIFT method=executeQuery guid=$guid")
    val paramsMap: Map[String, Any] = params.asScala.map(p => p.getName -> getParamValue(p)).toMap
    val ruid = requestLog.calledExecuteQuery(ExecuteQueryRequest("", guid, domainUdk, viewUdk, source, paramsMap))
    val request = ExecuteQueryRequest(ruid, guid, domainUdk, viewUdk, source, paramsMap)

    val (domain, views) = catalog.findDomainWithViewsByUdk(domainUdk) match {
      case Failure(ex) =>
      requestLog.finishedExecuteQuery(request.copy(status = tStatus.ExceptionStatus, exception = ex))
        return QueryResponse(ex)
      case Success(domainAndViews) => domainAndViews
    }

    views.find(_.udk.exists(viewUdk == _)) match {
      case None =>
        val failure = VitalsException(s"Domain<$domainUdk> does not contain View<$viewUdk>")
        requestLog.finishedExecuteQuery(request.copy(status = tStatus.ExceptionStatus, exception = failure))
        QueryResponse(failure)

      case Some(view) =>
        val response = agent.execute(source, FabricOver(domain.pk, view.pk, timezone), guid, calls.fromThrift(params))
          .map(result => {
            val group = result.resultGroup.map(group => results.toThrift(domainUdk, viewUdk, group))
            val logResults = group.map(r => r.resultSets.asScala.values.map(s => (s.name, s.rows.size)).toMap).getOrElse(Map.empty)
            requestLog.finishedExecuteQuery(request.copy(status = thriftStatus(result.resultStatus), results = logResults))
            QueryResponse(result, group)
          })
          .recover({
            case failure =>
              log.warn(s"CLIENT_THRIFT recovered from: ${failure.getMessage}")
              requestLog.finishedExecuteQuery(request.copy(status = tStatus.ExceptionStatus, exception = failure))
              QueryResponse(failure)
          })

        try {
          Await.result(response, 90 seconds)
        } catch safely {
          case t: TimeoutException =>
            requestLog.finishedExecuteQuery(request.copy(status = tStatus.TimeoutStatus, exception = t))
            QueryResponse(t)
        }
    }
  }

  private def getParamValue(param: BTParameter): Any = param.format match {
    case BTDataFormat.Scalar => DatumValue.extractVal(param.primaryType, param.datum)
    case BTDataFormat.Vector => DatumValue.extractVector(param.primaryType, param.datum)
    case BTDataFormat.Map => ???
  }

  private def getDomain(udk: String, onComplete: tStatus => Unit, onException: Throwable => Unit): BTDomainResponse = {
    catalog.findDomainByUdk(udk).map(domain => {
      val views = catalog.allViewsForDomain(domain.pk) match {
        case Failure(_) => Array.empty[CatalogView]
        case Success(views) => views
      }
      onComplete(tStatus.SuccessStatus)
      DomainResponse(domain, views)
    }).recover({
      case nf: CatalogNotFoundException =>
        onComplete(tStatus.NotFound)
        DomainResponse(nf)
      case t =>
        onException(t)
        DomainResponse(t)
    }).getOrElse({
      val failure = VitalsException(s"Unable to get domain by udk for unknown cause. (udk=${udk})")
      onException(failure)
      DomainResponse(failure)
    })
  }

}
