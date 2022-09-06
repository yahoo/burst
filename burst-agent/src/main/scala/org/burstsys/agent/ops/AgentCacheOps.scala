/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.ops

import org.burstsys.agent.model.cache.generation._
import org.burstsys.agent.model.cache.generation.key._
import org.burstsys.agent.model.cache.operator._
import org.burstsys.agent.model.cache.operator.parameter._
import org.burstsys.agent.{AgentService, AgentServiceContext}
import org.burstsys.api._
import org.burstsys.fabric.data.model.generation._
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.ops.{FabricCacheManageOp, FabricCacheOpParameter, FabricCacheOps}
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.uid._

import scala.concurrent.Future

trait AgentCacheOps extends FabricCacheOps with AgentService {

  self: AgentServiceContext =>

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _cache: FabricCacheOps = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // api
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def registerCache(cache: FabricCacheOps): this.type = {
    if (_cache != null) throw VitalsException(s"cache already registered")
    _cache = cache
    this
  }

  final
  def resetCache(): this.type = {
    _cache = null
    this
  }

  final override
  def cacheGenerationOp(guid: VitalsUid,
                        operation: FabricCacheManageOp,
                        generationKey: FabricGenerationKey,
                        parameters: Option[Seq[FabricCacheOpParameter]]
                       ): Future[Seq[FabricGeneration]] = {
    if (modality.isServer) {
      _cache.cacheGenerationOp(guid, operation, generationKey, parameters)
    } else {
      TeslaRequestFuture(parameters.map(_.map(fabricToThriftCacheParameter))) chainWithFuture { params =>
        apiClient.cacheOperation(Some(guid), operation, generationKey, params)
      } map { result =>
        result.generations.map(_.toSeq.map(thriftToFabricCacheGeneration))
          .getOrElse(Seq.empty[FabricGeneration])
      }
    }
  }

  final override
  def cacheSliceOp(guid: VitalsUid, generationKey: FabricGenerationKey): Future[Seq[FabricSliceMetadata]] = {
    if (modality.isServer) {
      _cache.cacheSliceOp(guid, generationKey)
    } else {
      // Should we make a thrift call and convert back from thrift to fabric here?
      ???
    }
  }
}
