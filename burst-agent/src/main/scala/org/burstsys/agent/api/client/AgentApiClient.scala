/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.api.client

import org.burstsys.agent.AgentService
import org.burstsys.agent.api.BurstQueryApiResultStatus.BurstQueryApiInvalidStatus
import org.burstsys.agent.api._
import org.burstsys.api.BurstApiClient
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupUid
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.errors._
import com.twitter.util.Future

import scala.language.{implicitConversions, postfixOps}
import org.burstsys.vitals.logging._


private[agent] final case
class AgentApiClient(service: AgentService, modality: VitalsServiceModality) extends BurstApiClient[BurstQueryApiService.MethodPerEndpoint] with AgentApi {

  override
  def groupExecute(
                    groupUid: Option[FabricGroupUid],
                    source: String,
                    over: BurstQueryApiOver,
                    call: Option[BurstQueryApiCall]
                  ): Future[BurstQueryApiExecuteResult] = {
    try {
      ensureRunning
      if (source == null || source.isEmpty) {
        return Future(BurstQueryApiExecuteResult(BurstQueryApiInvalidStatus, "No query source provided"))
      }
      thriftClient.groupExecute(groupUid, source, over, call)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }

  override
  def cacheOperation(
                      groupUid: Option[FabricGroupUid],
                      operation: BurstQueryCacheOperation,
                      generationKey: BurstQueryCacheGenerationKey,
                      parameters: Option[scala.collection.Seq[BurstQueryCacheOperationParameter]]
                    ): Future[BurstQueryCacheGenerationResult] = {
    try {
      ensureRunning
      thriftClient.cacheOperation(groupUid, operation, generationKey, parameters)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }

  override
  def sliceFetch(groupUid: Option[FabricGroupUid], generation: BurstQueryApiGenerationKey): Future[BurstQuerySliceGenerationResult] = {
    try {
      ensureRunning
      thriftClient.sliceFetch(groupUid, generation)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }

}
