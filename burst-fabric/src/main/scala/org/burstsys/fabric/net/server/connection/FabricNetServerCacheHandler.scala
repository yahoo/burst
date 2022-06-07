/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server.connection

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.ops.FabricCacheManageOp
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.net.message.cache
import org.burstsys.fabric.net.message.cache._
import org.burstsys.fabric.net.message.scatter.FabricNetScatterMsg
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.uid._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

/**
 * server side management of cache messages
 */
trait FabricNetServerCacheHandler extends AnyRef with FabricNetServerConnection {

  self: FabricNetServerConnectionContext =>

  ////////////////////////////////////////////////////////////////////////////////
  // TYPES
  ////////////////////////////////////////////////////////////////////////////////

  case
  class CacheManageOperationCall(request: FabricNetCacheOperationReqMsg)
    extends FabricNetCall[FabricNetCacheOperationReqMsg, FabricNetCacheOperationRespMsg, Array[FabricGeneration]]

  case
  class CacheSliceFetch(request: FabricNetSliceFetchReqMsg)
    extends FabricNetCall[FabricNetSliceFetchReqMsg, FabricNetSliceFetchRespMsg, Array[FabricSliceMetadata]]

  ////////////////////////////////////////////////////////////////////////////////
  // TENDER THREAD
  ////////////////////////////////////////////////////////////////////////////////

  final val slotTenderBackgrounder = new VitalsBackgroundFunction(
    "fabric-net-server-tender", 1 minute, 1 minute, {
      // TODO handle timeouts...
    })

  ////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _cacheOperations = new ConcurrentHashMap[FabricNetScatterMsg, CacheManageOperationCall]

  private[this]
  val _fetchOperations = new ConcurrentHashMap[FabricNetScatterMsg, CacheSliceFetch]

  ////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////

  final override
  def cacheManageOperation(guid: VitalsUid, ruid: VitalsUid, operation: FabricCacheManageOp, generationKey: FabricGenerationKey): Future[Array[FabricGeneration]] = {
    val slot = CacheManageOperationCall(FabricNetCacheOperationReqMsg(guid, ruid, serverKey, clientKey, operation, generationKey))
    _cacheOperations.put(slot.request, slot)
    transmitter.transmitControlMessage(slot.request)
    slot.receipt.future
  }

  final override
  def cacheSliceFetchOperation(guid: VitalsUid, ruid: VitalsUid, generationKey: FabricGenerationKey): Future[Array[FabricSliceMetadata]] = {
    val slot = CacheSliceFetch(cache.FabricNetSliceFetchReqMsg(guid, ruid, serverKey, clientKey, generationKey))
    _fetchOperations.put(slot.request, slot)
    transmitter.transmitControlMessage(slot.request)
    slot.receipt.future
  }

  ////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ////////////////////////////////////////////////////////////////////////////////

  final protected
  def cacheManageOperationResp(msg: FabricNetCacheOperationRespMsg): Unit = {
    val slot = _cacheOperations.get(msg)
    if (slot == null) log warn s"$msg SLOTLESS"
    else {
      _cacheOperations.remove(msg)
      slot.receipt.complete(Success(msg.generations))
    }
  }

  final protected
  def cacheSliceFetchOperationResp(msg: FabricNetSliceFetchRespMsg): Unit = {
    val slot = _fetchOperations.get(msg)
    if (slot == null) log warn s"$msg SLOTLESS"
    else {
      _fetchOperations.remove(msg)
      slot.receipt.complete(Success(msg.slices))
    }
  }

}
