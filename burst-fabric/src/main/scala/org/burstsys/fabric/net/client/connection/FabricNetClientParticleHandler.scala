/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client.connection

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.fabric.exception.{FabricException, FabricGenericException}
import org.burstsys.fabric.execution.FabricExecutionException
import org.burstsys.fabric.execution.model.pipeline.{FabricPipelineEvent, FabricPipelineEventListener}
import org.burstsys.fabric.net.message.scatter.FabricNetProgressMsg
import org.burstsys.fabric.net.message.wave.{FabricNetParticleReqMsg, FabricNetParticleRespMsg}
import org.burstsys.fabric.trek.FabricWorkerRequestTrekMark
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors._
import org.burstsys.vitals.uid.VitalsUid

import scala.language.postfixOps
import scala.util.{Failure, Success}
import org.burstsys.vitals.logging._

/**
 * A single return of a wave result from a worker (client) to the master (server)
 */
trait FabricNetClientParticleHandler extends FabricPipelineEventListener {

  self: FabricNetClientConnectionContext =>

  private[this]
  val _guidToRuid: ConcurrentHashMap[VitalsUid, VitalsUid] = new ConcurrentHashMap[VitalsUid, VitalsUid]()

  private[this]
  val _reportingGuids = ConcurrentHashMap.newKeySet[VitalsUid]()

  override def onEvent: PartialFunction[FabricPipelineEvent, Unit] = {
    case e: FabricLoadEvent =>
      val ruid = _guidToRuid.get(e.guid)
      if (ruid == null) log debug s"no ruid for ${e.guid}"
      else {
        if (_reportingGuids.contains(e.guid))
          transmitter.transmitControlMessage(FabricNetProgressMsg(clientKey, serverKey, e.guid, ruid, e.eventId, e.nanos, e.store, e.event))
      }

    case e: FabricExecutionEvent =>
      val ruid = _guidToRuid.get(e.guid)
      if (ruid == null) log debug s"no ruid for ${e.guid}"
      else {
        if (_reportingGuids.contains(e.guid))
          transmitter.transmitControlMessage(FabricNetProgressMsg(clientKey, serverKey, e.guid, ruid, e.eventId, e.nanos))
      }

  }

  /**
   * local client-worker has an incoming particle request
   */
  final
  def executeParticle(msg: FabricNetParticleReqMsg): Unit = {
    val guid = msg.particle.slice.guid
    val ruid = msg.ruid
    val tag = s"FabricNetClientParticleHandler.executeParticle(guid=$guid, ruid=$ruid)"
    try {
      FabricWorkerRequestTrekMark.begin(guid)
      _guidToRuid.put(guid, ruid)
      if (msg.particle.instrumented)
        _reportingGuids.add(guid)
      if (client.engine == null) throw FabricExecutionException(s"$tag no engine configured for worker")
      log debug s"$tag executing particle ${msg.particle.slice.datasource} slice=${msg.particle.slice.sliceKey}"
      val gather = client.engine.executionParticleOp(ruid, msg.particle)
      // TODO - where do we compress these...
      transmitter.transmitDataMessage(FabricNetParticleRespMsg(msg, clientKey, serverKey, gather)) onComplete {
        case Success(_) => FabricWorkerRequestTrekMark.end(guid)
        case Failure(_) => FabricWorkerRequestTrekMark.fail(guid)
      }

    } catch safely {

      // transmit back fabric related exception for master to sort out
      case t: FabricException =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        FabricWorkerRequestTrekMark.fail(guid)
        transmitter transmitControlMessage FabricNetParticleRespMsg(msg, clientKey, serverKey, t)

      // something unpredictable happened...
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        FabricWorkerRequestTrekMark.fail(guid)
        transmitter transmitControlMessage FabricNetParticleRespMsg(msg, clientKey, serverKey, FabricGenericException(t))

    } finally {
      _guidToRuid.remove(guid)
      _reportingGuids.remove(guid)
    }
  }

}
