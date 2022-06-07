/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server.connection

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.fabric.execution.model.wave.FabricParticle
import org.burstsys.fabric.net.message.scatter.{FabricNetProgressMsg, FabricNetScatterMsg}
import org.burstsys.fabric.net.message.wave.{FabricNetParticleReqMsg, FabricNetParticleRespMsg}
import org.burstsys.fabric.trek.FabricMasterRequestTrekMark
import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.tesla.thread.request.teslaRequestExecutor

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * A per client (worker) connection wave transmitter. Note that a given wave will have one
 * of these particle transmissions per worker/slice. Spark uses special protocols to optimize this sort of
 * redundant network traffic but we are not sure with Hydra its necessary. At this level
 * this is a purely async message - any sort of understanding and management of the set of active
 * workers involved in a wave is managed at a high level in the fabric layer.
 */
trait FabricNetServerParticleHandler {

  self: FabricNetServerConnection =>

  ////////////////////////////////////////////////////////////////////////////////
  // TYPES
  ////////////////////////////////////////////////////////////////////////////////

  case
  class ExecutionParticleOpCall(request: FabricNetParticleReqMsg)
    extends FabricNetCall[FabricNetParticleReqMsg, FabricNetParticleRespMsg, FabricGather]

  ////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _particleCallMap = new ConcurrentHashMap[FabricNetScatterMsg, ExecutionParticleOpCall]

  private[this]
  val _particleSlotMap = new ConcurrentHashMap[FabricNetScatterMsg, TeslaScatterSlot]

  ////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////

  final override
  def executeParticle(slot: TeslaScatterSlot, particle: FabricParticle): Future[FabricGather] = {
    val guid = slot.scatter.guid
    val ruid = slot.ruid
    val tag = s"FabricNetServerParticleHandler.executeParticle(guid=$guid, ruid=$ruid)"
    val msg = FabricNetParticleReqMsg(guid, ruid, serverKey, clientKey, particle)
    val call = ExecutionParticleOpCall(msg)
    _particleCallMap.put(msg, call)
    _particleSlotMap.put(msg, slot)
    transmitter.transmitControlMessage(call.request) onComplete {
      case Success(_) =>
        FabricMasterRequestTrekMark.begin(guid) // are you there yet?
      case Failure(t) =>
        log warn s"FAB_NET_PARTICLE_XMIT_FAIL $t $tag"
    }
    call.receipt.future
  }

  ////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  ////////////////////////////////////////////////////////////////////////////////

  final protected
  def particleExecutionProgress(msg: FabricNetProgressMsg): Unit = {
    val tag = s"FabricNetServerParticleHandler.particleExecutionProgress($msg)"
    val slot = _particleSlotMap.get(msg)
    if (slot == null) log warn s"$tag SLOTLESS"
    else slot.slotProgress(msg.scatterMsg)
  }

  final protected
  def particleExecutionResp(msg: FabricNetParticleRespMsg): Unit = {
    val tag = s"FabricNetServerParticleHandler.particleExecutionResp($msg)"
    val slot = _particleCallMap.get(msg)
    if (slot == null) log warn s"$tag SLOTLESS"
    else {
      FabricMasterRequestTrekMark.end(msg.guid) // are you there yet?
      log debug s"$tag received result"
      _particleCallMap.remove(msg)
      _particleSlotMap.remove(msg)
      slot.receipt.complete(msg.result)
    }
  }

}
