/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.processors

import org.burstsys.agent.{AgentLanguage, AgentService}
import org.burstsys.catalog.CatalogService
import org.burstsys.eql.EqlContext
import org.burstsys.fabric.exception.FabricQueryProcessingException
import org.burstsys.fabric.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.logging.burstStdMsg

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Parse EQL into hydra and then delegate back to the agent to execute the hydra
 */
final case
class BurstSystemEqlQueryProcessor(agent: AgentService, catalog: CatalogService) extends AgentLanguage {

  override val languagePrefixes: Array[String] = Array("select", "funnel", "segment")

  override
  def executeGroupAsWave(groupUid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall]): Future[FabricExecuteResult] = {
    val tag = s"BurstSystemEqlQueryProcessor.executeGroupAsWave(guid=$groupUid, $over)"
    log info s"""BURST_AGENT_PROCESSORS $tag parsing source language='EQL' query="${singleLineSource(source)}""""

    TeslaRequestFuture {
      val viewSchemaName = catalog.findViewByPk(over.viewKey) match {
        case Failure(e) => throw e
        case Success(view) => view.schemaName.toLowerCase.trim
      }
      Try(EqlContext(groupUid).eqlToHydra(Some(viewSchemaName), source.trim)) match {
        case Failure(e) => throw FabricQueryProcessingException("EQL", s"$tag Unable to parse EQL ${e.getMessage}", e)
        case Success(hydraSource) => hydraSource
      }
    } chainWithFuture { hydra =>
      agent.delegateLanguage(groupUid, hydra, over, call) // then send it into the agent again for further evaluation
    }
  }

}
