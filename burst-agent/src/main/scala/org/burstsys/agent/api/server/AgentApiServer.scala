/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.api.server

import com.twitter.util.{Future => TwitterFuture}
import org.burstsys.agent.AgentService
import org.burstsys.agent.api.BurstQueryApiResultStatus.BurstQueryApiExceptionStatus
import org.burstsys.agent.api._
import org.burstsys.agent.api.AgentApi
import org.burstsys.agent.api.BurstQueryApiCall
import org.burstsys.agent.model.cache.AgentGenerationResult
import org.burstsys.agent.model.cache.AgentSlicesResult
import org.burstsys.agent.model.cache.generation._
import org.burstsys.agent.model.cache.generation.key._
import org.burstsys.agent.model.cache.operator._
import org.burstsys.agent.model.cache.operator.parameter._
import org.burstsys.agent.model.cache.slice._
import org.burstsys.agent.model.execution.group.call.thriftToFabricCall
import org.burstsys.agent.model.execution.group.over.AgentThriftOver
import org.burstsys.agent.model.execution.group.over._
import org.burstsys.agent.model.execution.result.ThriftAgentExecuteResult
import org.burstsys.agent.model.execution.result._
import org.burstsys.api.BurstApiServer
import org.burstsys.api._
import org.burstsys.fabric.wave.data.model.ops.FabricCacheOpParameter
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.wave.execution.model.execute.group.sanitizeGuid
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.uid.VitalsUid
import org.burstsys.vitals.uid.newBurstUid

import java.util.concurrent.TimeUnit
import scala.concurrent.Promise
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success

private[agent] final case
class AgentApiServer(service: AgentService, modality: VitalsServiceModality) extends BurstApiServer with AgentApi {

  override def groupExecute(groupUid: Option[FabricGroupUid], source: String, over: AgentThriftOver, call: Option[BurstQueryApiCall]): TwitterFuture[ThriftAgentExecuteResult] = {
    val start = System.nanoTime
    TeslaRequestFuture {
      ensureRunning
      sanitizeGuid(groupUid)
    } chainWithFuture { guid =>
      if (!groupUid.exists(g => guid.startsWith(g))) {
        log info s"AGENT_THRIFT_GUID_INVALID provided='${groupUid.orNull}' guid='$guid'"
      }
      service.execute(source, over, guid, call.map(thriftToFabricCall)) map fabricToAgentExecuteResult
    } andThen {
      case _ =>
        log info s"THRIFT_API agent request complete duration=${TimeUnit.NANOSECONDS.toMillis(System.nanoTime - start)}"
    }
  }

  override def cacheOperation(
                      guid: Option[VitalsUid],
                      operation: BurstQueryCacheOperation,
                      generationKey: BurstQueryCacheGenerationKey,
                      parameters: Option[scala.collection.Seq[BurstQueryCacheOperationParameter]]
                    ): TwitterFuture[BurstQueryCacheGenerationResult] = {
    ensureRunning
    val promise = Promise[BurstQueryCacheGenerationResult]()
    val params = parameters.map(_.toSeq.map(p => p: FabricCacheOpParameter))
    service.cacheGenerationOp(guid.getOrElse(newBurstUid), operation, generationKey, params) onComplete {
      case Failure(t) =>
        promise.success(AgentGenerationResult(BurstQueryApiExceptionStatus, burstStdMsg(t)))
      case Success(s) =>
        promise.success(AgentGenerationResult(generations = Some(s.map(g => g: BurstQueryCacheGeneration))))
    }
    promise.future
  }

  final override
  def sliceFetch(guid: Option[VitalsUid], generation: BurstQueryApiGenerationKey): TwitterFuture[BurstQuerySliceGenerationResult] = {
    ensureRunning
    val promise = Promise[BurstQuerySliceGenerationResult]()
    service.cacheSliceOp(guid.getOrElse(newBurstUid), generation) onComplete {
      case Failure(t) => promise.success(AgentSlicesResult(burstStdMsg(t)))
      case Success(s) => promise.success(AgentSlicesResult(s.map(slice => slice: BurstQueryCacheSlice)))
    }
    promise.future
  }
}
