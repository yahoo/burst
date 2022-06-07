/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.api.server

import org.burstsys.agent.api.BurstQueryApiResultStatus.BurstQueryApiExceptionStatus
import org.burstsys.agent.api._
import org.burstsys.agent.model.cache._
import org.burstsys.agent.model.cache.generation._
import org.burstsys.agent.model.cache.generation.key._
import org.burstsys.agent.model.cache.operator._
import org.burstsys.agent.model.cache.operator.parameter._
import org.burstsys.agent.model.cache.slice._
import org.burstsys.api._
import org.burstsys.fabric.data.model.ops.FabricCacheOpParameter
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid._
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.Promise
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait AgentCacheReactor extends AgentApi {
  self: AgentApiServer =>

  final override
  def cacheOperation(
                      guid: Option[VitalsUid],
                      operation: BurstQueryCacheOperation,
                      generationKey: BurstQueryCacheGenerationKey,
                      parameters: Option[Seq[BurstQueryCacheOperationParameter]]
                    ): TwitterFuture[BurstQueryCacheGenerationResult] = {
    ensureRunning
    val promise = Promise[BurstQueryCacheGenerationResult]
    val params = parameters.map(_.map(p => p: FabricCacheOpParameter))
    service.cacheGenerationOp(guid.getOrElse(newBurstUid), operation, generationKey, params) onComplete {
      case Failure(t) => promise.success(AgentGenerationResult(BurstQueryApiExceptionStatus, burstStdMsg(t)))
      case Success(s) => promise.success(AgentGenerationResult(generations = Some(s.map(g => g: BurstQueryCacheGeneration))))
    }
    promise.future
  }

  final override
  def sliceFetch(guid: Option[VitalsUid], generation: BurstQueryApiGenerationKey): TwitterFuture[BurstQuerySliceGenerationResult] = {
    ensureRunning
    val promise = Promise[BurstQuerySliceGenerationResult]
    service.cacheSliceOp(guid.getOrElse(newBurstUid), generation) onComplete {
      case Failure(t) => promise.success(AgentSlicesResult(burstStdMsg(t)))
      case Success(s) => promise.success(AgentSlicesResult(s.map(slice => slice: BurstQueryCacheSlice)))
    }
    promise.future
  }
}

