/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent

import org.burstsys.agent.api.client.AgentApiClient
import org.burstsys.agent.api.server.AgentApiServer
import org.burstsys.agent.event.AgentEventTalker
import org.burstsys.agent.ops.{AgentCacheOps, AgentExecuteOps}
import org.burstsys.agent.transform.AgentTransform
import org.burstsys.fabric.data.model.ops.FabricCacheOps
import org.burstsys.fabric.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandardClient, VitalsStandardServer}
import org.burstsys.vitals.healthcheck.VitalsHealthMonitoredService

import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait AgentService extends VitalsService with FabricCacheOps {

  /**
   * Add a processor for queries in a burst language. (e.g. eql, hydra)
   *
   * (only done during supervisor startup and in torquemada)
   *
   * @param processor a language processor
   */
  def registerLanguage(processor: AgentLanguage): this.type

  /**
   * Register a cache manager
   *
   * (only done during supervisor startup and in torquemada)
   *
   * @param processor a cache manager
   */
  def registerCache(processor: FabricCacheOps): this.type

  /**
   * take result sets and transforms them.
   *
   * This is currently a no-op as there is no way to specify a transform, and transforms can do nothing
   *
   * @param transform a results transformer
   * @param results   the results of a query
   * @return the transformed result set
   */
  def queryTransform(transform: AgentTransform, results: FabricExecuteResult): Future[FabricExecuteResult]

  /**
   * Execute a query against a particular dataset.
   *
   * @param source the textual representation of the query
   * @param over   the domain and view to run the query against
   * @param guid   a guid for this query run
   * @param call   parameters for query execution
   * @return the query results
   */
  def execute(source: String, over: FabricOver, guid: FabricGroupUid, call: Option[FabricCall] = None): Future[FabricExecuteResult]

  def delegateLanguage(guid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall] = None): Future[FabricExecuteResult]
}

object AgentService {

  def apply(mode: VitalsServiceModality = VitalsStandardClient): AgentService = AgentServiceContext(mode: VitalsServiceModality)

}

private[agent] final case
class AgentServiceContext(modality: VitalsServiceModality) extends AgentService
  with AgentExecuteOps with AgentCacheOps with AgentEventTalker with VitalsHealthMonitoredService {

  override val serviceName: String = s"agent($modality)"

  val requestTimeout: Duration = configuration.burstAgentApiTimeoutDuration

  val apiClient: AgentApiClient = AgentApiClient(this, VitalsStandardClient)

  val apiServer: AgentApiServer = AgentApiServer(this, VitalsStandardServer)

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    if (modality.isServer) {
      apiServer.start
    } else {
      apiClient.start
    }
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    if (modality.isServer) {
      apiServer.stop
    } else {
      apiClient.stop
    }
    resetCache()
    markNotRunning
    this
  }

}
