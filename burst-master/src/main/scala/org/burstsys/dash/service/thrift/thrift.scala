/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.service

import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogExceptions.CatalogNotFoundException
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.model.domain.CatalogDomain
import org.burstsys.catalog.model.view.CatalogView
import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.execution.model.result.status.FabricResultStatus
import org.burstsys.fabric.execution.model.result.{status => fStatus}
import org.burstsys.gen.thrift.api.client.BTBurstService
import org.burstsys.gen.thrift.api.client.BTBurstService.Iface
import org.burstsys.gen.thrift.api.client.BTRequestOutcome
import org.burstsys.gen.thrift.api.client.domain.BTDomainResponse
import org.burstsys.gen.thrift.api.client.query.BTQueryResponse
import org.burstsys.gen.thrift.api.client.query.BTResult
import org.burstsys.gen.thrift.api.client.view.BTViewResponse
import org.burstsys.gen.thrift.api.client.{BTResultStatus => tStatus}
import org.burstsys.vitals.logging.VitalsLogger

import java.util
import scala.collection.JavaConverters._
import scala.concurrent.TimeoutException

package object thrift extends VitalsLogger {

  val requestLog: ThriftRequestLog = ThriftRequestLog()

  def processor(catalog: CatalogService, agent: AgentService): BTBurstService.Processor[Iface] = {
    new BTBurstService.Processor[BurstRestThriftResponder](BurstRestThriftResponder(catalog, agent)).asInstanceOf[BTBurstService.Processor[Iface]]
  }

  def emptyPropMap: util.Map[String, String] = Map.empty[String, String].asJava

  def asMap[K, V](fromJava: util.Map[K, V]): Map[K, V] = fromJava.asScala.toMap

  def bailWith(message: String): Nothing = {
    log.warn(message)
    ???
  }

  private def success(message: String): BTRequestOutcome =
    new BTRequestOutcome(tStatus.SuccessStatus, message)

  private def timeout(message: String): BTRequestOutcome =
    new BTRequestOutcome(tStatus.TimeoutStatus, message)

  private def notFound(message: String): BTRequestOutcome =
    new BTRequestOutcome(tStatus.NotFound, message)

  private def exception(message: String): BTRequestOutcome =
    new BTRequestOutcome(tStatus.ExceptionStatus, message)

  def thriftStatus(forStatus: FabricResultStatus): tStatus = forStatus match {
    case fStatus.FabricInProgressResultStatus => tStatus.InProgressStatus
    case fStatus.FabricUnknownResultStatus => tStatus.UnknownStatus
    case fStatus.FabricSuccessResultStatus => tStatus.SuccessStatus
    case fStatus.FabricFaultResultStatus => tStatus.ExceptionStatus
    case fStatus.FabricInvalidResultStatus => tStatus.InvalidStatus
    case fStatus.FabricTimeoutResultStatus => tStatus.TimeoutStatus
    case fStatus.FabricNotReadyResultStatus => tStatus.NotReadyStatus
    case fStatus.FabricNoDataResultStatus => tStatus.NoDataStatus
    case fStatus.FabricStoreErrorResultStatus => tStatus.StoreErrorStatus
    case unknown => bailWith(s"new fabric result status ${unknown}")
  }

  private def outcomeFor(result: FabricExecuteResult): BTRequestOutcome =
    new BTRequestOutcome(thriftStatus(result.resultStatus), result.resultMessage)

  object DomainResponse {
    def apply(domain: CatalogDomain, viewArray: Array[CatalogView]): BTDomainResponse = {
      val response = new BTDomainResponse(success(s"Found domain '${domain.udk.orNull}' with ${viewArray.length} view(s)"), emptyPropMap)
      val tDomain = domains.toThriftDomain(domain)
      tDomain.setViews(viewArray.map(views.toThriftView(tDomain.udk, _)).toList.asJava)
      response.setDomain(tDomain)
      response
    }

    def apply(failure: Throwable): BTDomainResponse = failure match {
      case nf: CatalogNotFoundException =>
        new BTDomainResponse(notFound(s"Unable to find domain: ${failure.getLocalizedMessage}"), emptyPropMap)
      case t =>
        new BTDomainResponse(exception(s"An error occurred: ${failure.getLocalizedMessage}"), emptyPropMap)
    }
  }

  object ViewResponse {
    def apply(domainUdk: String, view: CatalogView): BTViewResponse = {
      val response = new BTViewResponse(success(s"Found view '${view.udk.orNull}''"), emptyPropMap)
      response.setView(views.toThriftView(domainUdk, view))
      response
    }

    def apply(domainUdk: String, viewArray: Array[CatalogView]): BTViewResponse = {
      val response = new BTViewResponse(success(s"Found ${viewArray.length} view(s)"), emptyPropMap)
      response.setViews(viewArray.map(views.toThriftView(domainUdk, _)).toList.asJava)
      response
    }

    def apply(failure: Throwable): BTViewResponse = {
      new BTViewResponse(exception(s"An error occurred: ${failure.getLocalizedMessage}"), emptyPropMap)
    }
  }

  object QueryResponse {
    def apply(result: FabricExecuteResult, group: Option[BTResult]): BTQueryResponse = {
      val response = new BTQueryResponse(outcomeFor(result), emptyPropMap)
      group.map(response.setResult)
      response
    }

    def apply(failure: Throwable): BTQueryResponse = {
      val outcome = failure match {
        case _: TimeoutException => timeout(s"A timeout occurred: ${failure.getLocalizedMessage}")
        case _ => exception(s"An error occurred: ${failure.getLocalizedMessage}")
      }
      new BTQueryResponse(outcome, emptyPropMap)
    }
  }
}
