/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.container.supervisor

import org.burstsys.fabric.container.supervisor.{FabricSupervisorContainer, FabricSupervisorContainerContext, FabricSupervisorListener}
import org.burstsys.fabric.wave.data.model.generation.FabricGeneration
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.ops.FabricCacheManageOp
import org.burstsys.fabric.wave.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.wave.data.supervisor.FabricSupervisorData
import org.burstsys.fabric.wave.data.supervisor.store._
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.wave.FabricParticle
import org.burstsys.fabric.wave.execution.supervisor.FabricSupervisorExecution
import org.burstsys.fabric.wave.metadata.supervisor.FabricSupervisorMetadata
import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetHeartbeatMsg}
import org.burstsys.fabric.wave.message.cache.{FabricNetCacheOperationReqMsg, FabricNetCacheOperationRespMsg, FabricNetSliceFetchReqMsg, FabricNetSliceFetchRespMsg}
import org.burstsys.fabric.wave.message.scatter.{FabricNetProgressMsg, FabricNetScatterMsg}
import org.burstsys.fabric.wave.message.wave.{FabricNetParticleReqMsg, FabricNetParticleRespMsg}
import org.burstsys.fabric.wave.message.{FabricNetCacheOperationRespMsgType, FabricNetParticleRespMsgType, FabricNetProgressMsgType, FabricNetSliceFetchRespMsgType, cache}
import org.burstsys.fabric.net.server.connection.{FabricNetCall, FabricNetServerConnection}
import org.burstsys.fabric.net.{FabricNetworkConfig, message}
import org.burstsys.fabric.wave.trek.FabricSupervisorRequestTrekMark
import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.uid.VitalsUid

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * the one per JVM top level container for a Fabric Supervisor
 */
trait FabricWaveSupervisorContainer extends FabricSupervisorContainer[FabricWaveSupervisorListener] with FabricWaveSupervisorAPI {

  /**
   * the supervisor data service
   *
   * @return
   */
  def data: FabricSupervisorData

  /**
   * the supervisor metadata service
   *
   * @return
   */
  def metadata: FabricSupervisorMetadata

  /**
   * the supervisor execution service
   *
   * @return
   */
  def execution: FabricSupervisorExecution
}

abstract
class FabricWaveSupervisorContainerContext(netConfig: FabricNetworkConfig) extends FabricSupervisorContainerContext[FabricWaveSupervisorListener](netConfig)
  with FabricWaveSupervisorContainer {

  override def serviceName: String = s"fabric-wave-supervisor-container"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _data: FabricSupervisorData = FabricSupervisorData(this)

  private[this]
  val _metadata: FabricSupervisorMetadata = FabricSupervisorMetadata(this)

  private[this]
  val _execution: FabricSupervisorExecution = FabricSupervisorExecution(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // hookups
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def data: FabricSupervisorData = _data

  override
  def metadata: FabricSupervisorMetadata = _metadata

  override
  def execution: FabricSupervisorExecution = _execution

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    synchronized {
      ensureNotRunning

      if (containerId.isEmpty) {
        containerId = System.currentTimeMillis()
      }

      // make sure that subtype has already defined metadata lookup and stores before calling this super start

      // start generic container
      super.start

      // we now have metadata access from top level container

      // start up the remaining four pillars net, topology, data, and execution
      _data.start
      _metadata.start
      _execution.start

      // start supervisor stores found in classpath
      startSupervisorStores(this)

      markRunning
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning

      _data.stop
      _metadata.stop
      _execution.stop

      // stop generic container
      super.stop

      // stop supervisor stores found in classpath
      stopSupervisorStores(this)

      markNotRunning
    }
    this
  }

  ////////////////////////////////////////////////////////////////////////////////
  // TYPES
  ////////////////////////////////////////////////////////////////////////////////

  case class CacheManageOperationCall(request: FabricNetCacheOperationReqMsg)
    extends FabricNetCall[FabricNetCacheOperationReqMsg, FabricNetCacheOperationRespMsg, Array[FabricGeneration]]

  case class CacheSliceFetch(request: FabricNetSliceFetchReqMsg)
    extends FabricNetCall[FabricNetSliceFetchReqMsg, FabricNetSliceFetchRespMsg, Array[FabricSliceMetadata]]

  case class ExecutionParticleOpCall(request: FabricNetParticleReqMsg)
    extends FabricNetCall[FabricNetParticleReqMsg, FabricNetParticleRespMsg, FabricGather]

  ////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _cacheOperations = new ConcurrentHashMap[FabricNetScatterMsg, CacheManageOperationCall]

  private[this]
  val _fetchOperations = new ConcurrentHashMap[FabricNetScatterMsg, CacheSliceFetch]

  private[this]
  val _particleCallMap = new ConcurrentHashMap[FabricNetScatterMsg, ExecutionParticleOpCall]

  private[this]
  val _particleSlotMap = new ConcurrentHashMap[FabricNetScatterMsg, TeslaScatterSlot]

  ////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////

  final override
  def cacheManageOperation(connection: FabricNetServerConnection, guid: VitalsUid, ruid: VitalsUid, operation: FabricCacheManageOp, generationKey: FabricGenerationKey): Future[Array[FabricGeneration]] = {
    val slot = CacheManageOperationCall(FabricNetCacheOperationReqMsg(guid, ruid, connection.serverKey, connection.clientKey, operation, generationKey))
    _cacheOperations.put(slot.request, slot)
    connection.transmitControlMessage(slot.request)
    slot.receipt.future
  }

  final override
  def cacheSliceFetchOperation(connection: FabricNetServerConnection, guid: VitalsUid, ruid: VitalsUid, generationKey: FabricGenerationKey): Future[Array[FabricSliceMetadata]] = {
    val slot = CacheSliceFetch(cache.FabricNetSliceFetchReqMsg(guid, ruid, connection.serverKey, connection.clientKey, generationKey))
    _fetchOperations.put(slot.request, slot)
    connection.transmitControlMessage(slot.request)
    slot.receipt.future
  }

  final override
  def executeParticle(connection: FabricNetServerConnection, slot: TeslaScatterSlot, particle: FabricParticle): Future[FabricGather] = {
    val guid = slot.scatter.guid
    val ruid = slot.ruid
    val tag = s"FabricWaveSupervisorContainer.executeParticle(guid=$guid, ruid=$ruid)"
    val msg = FabricNetParticleReqMsg(guid, ruid, connection.serverKey, connection.clientKey, particle)
    val call = ExecutionParticleOpCall(msg)
    slot.setSpan(FabricSupervisorRequestTrekMark.begin(guid, msg.ruid)) // are you there yet?
    _particleCallMap.put(msg, call)
    _particleSlotMap.put(msg, slot)
    connection.transmitControlMessage(call.request) onComplete {
      case Success(_) =>
        slot.span.addEvent("transmitControlMessage.success")
      case Failure(t) =>
        log warn s"FAB_NET_PARTICLE_XMIT_FAIL $t $tag"
        FabricSupervisorRequestTrekMark.fail(slot.span)
    }
    call.receipt.future
  }

  override def onNetMessage(connection: FabricNetServerConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {

    messageId match {
      /////////////////// PARTICLES /////////////////
      case FabricNetProgressMsgType =>
        val msg = FabricNetProgressMsg(buffer)
        filteredForeach[FabricWaveSupervisorListener](_.onNetServerParticleProgressMsg(connection, msg))

        val tag = s"FabricWaveSupervisorContainer.particleExecutionProgress($msg)"
        val slot = _particleSlotMap.get(msg)
        if (slot == null)
          log warn s"$tag SLOTLESS"
        else
          slot.slotProgress(msg.scatterMsg)

      case FabricNetParticleRespMsgType =>
        val msg = FabricNetParticleRespMsg(buffer)

        log debug s"FabricWaveSupervisorContainer.onNetServerParticleRespMsg $connection $msg"
        filteredForeach[FabricWaveSupervisorListener](_.onNetServerParticleRespMsg(connection, msg))

        val tag = s"FabricWaveSupervisorContainer.particleExecutionResp($msg)"
        val call = _particleCallMap.get(msg)
        val slot = _particleSlotMap.get(msg)
        if (call == null)
          log warn s"$tag SLOTLESS"
        else {
          FabricSupervisorRequestTrekMark.end(slot.span) // are you there yet?
          log debug s"$tag received result"
          _particleCallMap.remove(msg)
          _particleSlotMap.remove(msg)
          call.receipt.complete(msg.result)
        }

      /////////////////// CACHE OPERATIONS /////////////////
      case FabricNetCacheOperationRespMsgType =>
        val msg = FabricNetCacheOperationRespMsg(buffer)

        filteredForeach[FabricWaveSupervisorListener](_.onNetServerCacheOperationRespMsg(connection, msg))
        val slot = _cacheOperations.get(msg)
        if (slot == null) log warn s"$msg SLOTLESS"
        else {
          _cacheOperations.remove(msg)
          slot.receipt.complete(Success(msg.generations))
        }

      /////////////////// CACHE SLICE FETCH /////////////////
      case FabricNetSliceFetchRespMsgType =>
        val msg = FabricNetSliceFetchRespMsg(buffer)

        filteredForeach[FabricWaveSupervisorListener](_.onNetServerSliceFetchRespMsg(connection, msg))
        val slot = _fetchOperations.get(msg)
        if (slot == null) log warn s"$msg SLOTLESS"
        else {
          _fetchOperations.remove(msg)
          slot.receipt.complete(Success(msg.slices))
        }

      case mt =>
        log warn burstStdMsg(s"Unknown message type $mt")
        throw VitalsException(s"Supervisor receieved unknown message mt=$mt")
    }
  }

  override def onDisconnect(connection: FabricNetServerConnection): Unit = {
    filteredForeach[FabricSupervisorListener](_.onDisconnect(connection))
  }

  override def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetHeartbeatMsg): Unit = {
    filteredForeach[FabricSupervisorListener](_.onNetServerTetherMsg(connection,msg))
  }

  override def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {
    filteredForeach[FabricSupervisorListener](_.onNetServerAssessRespMsg(connection,msg))
  }
}

