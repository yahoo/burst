/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.execution

import java.util.concurrent.{ConcurrentHashMap, ConcurrentSkipListMap}
import java.util.function
import ExecutionState._
import org.burstsys.supervisor.http.websocket.log
import org.burstsys.fabric.wave.execution.supervisor.wave.FabricWaveSeqNum
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.gather.metrics.FabricGatherMetrics
import org.burstsys.fabric.wave.execution.model.result.status.{FabricResultStatus, FabricSuccessResultStatus}
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid.VitalsUid

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

trait RequestTracker {
  def requestStarted(guid: VitalsUid, traceId: String, source: String, over: FabricOver, call: Option[FabricCall]): VitalsUid

  def requestSucceeded(guid: VitalsUid): VitalsUid

  def requestFailed(guid: VitalsUid, status: FabricResultStatus, msg: String): VitalsUid

  def waveStarted(guid: VitalsUid, seqNum: FabricWaveSeqNum): VitalsUid

  def waveTimedout(guid: VitalsUid, seqNum: FabricWaveSeqNum, msg: String): VitalsUid

  def waveSucceeded(guid: VitalsUid, seqNum: FabricWaveSeqNum, results: FabricGatherMetrics): VitalsUid

  def waveFailed(guid: VitalsUid, seqNum: FabricWaveSeqNum, msg: String): VitalsUid

  def particleStarted(seqNum: FabricWaveSeqNum, ruid: VitalsUid, host: String): VitalsUid

  def particleProgress(seqNum: FabricWaveSeqNum, ruid: VitalsUid, msg: String): VitalsUid

  def particleTardy(seqNum: FabricWaveSeqNum, ruid: VitalsUid): VitalsUid

  def particleCancelled(seqNum: FabricWaveSeqNum, ruid: VitalsUid): VitalsUid

  def particleSucceeded(seqNum: FabricWaveSeqNum, ruid: VitalsUid): VitalsUid

  def particleFailed(seqNum: FabricWaveSeqNum, ruid: VitalsUid, msg: String): VitalsUid

  def lastReset: Long

  def clear(): Unit

  def get(guid: VitalsUid): Option[RequestState]

  def foreach[T](work: RequestState => T): Unit
}

// Used to make the treemap sort properly
private[execution] final class RequestKey(val guid: VitalsUid, val startTime: Long = System.nanoTime) extends Comparable[RequestKey] {
  override def hashCode(): Int = guid.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case k: RequestKey => k.guid == guid
    case _ => false
  }

  // flip this comparison so that the natural ordering is from most to least recent
  override def compareTo(o: RequestKey): Int = if (guid == o.guid) 0 else -1 * startTime.compareTo(o.startTime)
}

object RequestKey {
  def apply(guid: VitalsUid): RequestKey = new RequestKey(guid)

}

class RequestTrackerContext extends RequestTracker {

  private val _emptyUid = "000000000000000000000000000000000_000000"
  private var _lastReset = System.currentTimeMillis()
  private val requests = new ConcurrentSkipListMap[RequestKey, RequestState]()
  // concurrentskiplistmap uses the `.compareTo`, not `.equals`, to look up values
  // that means that we need to use the same RequestKey (or a key with the same startTime, but realisitically the same key)
  private val requestKeys = new ConcurrentHashMap[VitalsUid, RequestKey]()
  private val waves = new ConcurrentHashMap[FabricWaveSeqNum, WaveState]()

  private val cleanup = new VitalsBackgroundFunction("request-log-cleanup", 1 minute, 1 minute, {
    val oldestQueryToRetain = System.currentTimeMillis - (24 hours).toMillis
    val oldestSuccessfulQueryToRetain = System.currentTimeMillis - (12 hours).toMillis
    val earliestQueryHistoryToRetain = System.currentTimeMillis - (30 minutes).toMillis

    def retainQueryHistory(req: RequestState): Boolean = req.endTime > earliestQueryHistoryToRetain

    def retainRequest(req: RequestState): Boolean = {
      if (!req.state.isComplete) true
      else if (req.state.isSuccess) req.endTime > oldestSuccessfulQueryToRetain
      else req.endTime > oldestQueryToRetain
    }

    // synchronize against concurrent update or serialize
    RequestTrackerContext.this.synchronized {
      var idx = 0
      val iter = requests.entrySet.iterator
      while (iter.hasNext) {
        val entry = iter.next()
        val req = entry.getValue

        if (!retainQueryHistory(req)) {
          if (req.source.length > 1) {
            req.source.remove(1, req.source.length - 1)
          }
        }

        if (idx > 999 || !retainRequest(req)) {
          log info s"request-log-cleanup removing request ${req.guid}"
          req.wave.foreach { w => waves.remove(w.seqNum) }
          requestKeys.remove(findKey(req.guid))
          iter.remove()
        }
        idx += 1
      }
    }
  })
  cleanup.start

  private def findKey(guid: VitalsUid): RequestKey = requestKeys.computeIfAbsent(guid, _ => RequestKey(guid))

  private def requestFinished(guid: VitalsUid, state: FabricResultStatus, message: String): VitalsUid = this.synchronized {
    requests.get(findKey(guid)) match {
      case null => // do nothing, the tracker might have been reset after the request started
      case request =>
        request.endTime = System.currentTimeMillis
        request.state = state
        request.status = message
    }
    guid
  }

  private def waveFinished(seqNum: FabricWaveSeqNum, state: ExecutionState, message: String): WaveState = this.synchronized {
    waves.get(seqNum) match {
      case null => null
      case wave =>
        wave.state = state
        wave.status = message
        wave.endMillis = System.currentTimeMillis
        wave.computeParticleSkew()
        wave
    }
  }

  private def particleUpdate(seqNum: FabricWaveSeqNum, ruid: VitalsUid, state: ExecutionState, status: String): VitalsUid = this.synchronized {
    waves.get(seqNum) match {
      case wave if wave.particles.containsKey(ruid) =>
        val particle = wave.particles.get(ruid) match {
          case null => throw new IllegalStateException(s"Wave has null particle for $ruid")
          case p => p
        }
        particle.state = state
        particle.message = status
        particle.endMillis = System.currentTimeMillis
        wave.updateSummary()
        wave.guid

      case _ =>
        log info s"Particle update for unknown wave: $seqNum"
        _emptyUid
    }
  }

  override def lastReset: Long = _lastReset

  override def requestStarted(guid: VitalsUid, traceId: String, source: String, over: FabricOver, call: Option[FabricCall]): VitalsUid = this.synchronized {
    val request = requests.computeIfAbsent(findKey(guid), _ => RequestState(guid, traceId, over, call))
    val trimmed = source.trim
    if (request.source.isEmpty || request.source.last != trimmed)
      request.source += trimmed
    guid
  }

  override def requestSucceeded(guid: VitalsUid): VitalsUid =
    requestFinished(guid, FabricSuccessResultStatus, "Request succeeded")

  override def requestFailed(guid: VitalsUid, status: FabricResultStatus, msg: String): VitalsUid =
    requestFinished(guid, status, msg)


  override def waveStarted(guid: VitalsUid, seqNum: FabricWaveSeqNum): VitalsUid = {
    lazy val tag = s"RequestTracker.waveStarted(seqNum=$seqNum, guid=$guid)"
    this.synchronized {
      val wave = WaveState(seqNum, guid)
      waves.put(seqNum, wave)
      requests.get(findKey(guid)) match {
        case null => log warn s"WAVE_START_NO_REQUEST $tag"
        case req =>
          req.status = "Executing Wave"
          req.wave = Some(wave)
      }
    }
    guid
  }

  override def waveTimedout(guid: VitalsUid, seqNum: FabricWaveSeqNum, msg: String): VitalsUid = {
    waveFinished(seqNum, failed, msg)
    guid
  }

  override def waveSucceeded(guid: VitalsUid, seqNum: FabricWaveSeqNum, results: FabricGatherMetrics): VitalsUid = {
    val wave = waveFinished(seqNum, succeeded, "Wave succeeded")
    if (wave != null)
      wave.metrics = Option(results.toJson)
    guid
  }

  override def waveFailed(guid: VitalsUid, seqNum: FabricWaveSeqNum, msg: String): VitalsUid = {
    waveFinished(seqNum, failed, msg)
    guid
  }

  override def particleStarted(seqNum: FabricWaveSeqNum, ruid: VitalsUid, host: String): VitalsUid = this.synchronized {
    waves.get(seqNum) match {
      case null => _emptyUid
      case wave =>
        wave.particles.put(ruid, ParticleState(ruid, System.currentTimeMillis, host, inProgress, "Particle started"))
        wave.updateSummary()
        wave.guid
    }
  }

  override def particleProgress(seqNum: FabricWaveSeqNum, ruid: VitalsUid, message: String): VitalsUid = {
    parseUpdate(message) match {
      case Failure(e) =>
        log info s"Failed parsing progress message ruid=$ruid message=$message $e"
        _emptyUid
      case Success(update) =>
        waves.get(seqNum) match {
          case wave if wave.particles.contains(ruid) =>
            wave.particles.get(ruid).updates.append(update)
            wave.guid
          case _ =>
            log info s"Received udpate for unknown request ruid=$ruid"
            _emptyUid
        }
    }
  }

  private def parseUpdate(message: String): Try[ParticleState.Update] = {
    Try {
      val pieces = message.split(" \\| ")
      val nanos = pieces(0).toLong
      val name = pieces(1)
      val args = if (pieces.length > 2) pieces.slice(2, pieces.length) else Array.empty[String]
      ParticleState.Update(System.currentTimeMillis, nanos, name, args)
    }
  }

  override def particleTardy(seqNum: FabricWaveSeqNum, ruid: VitalsUid): VitalsUid = {
    particleUpdate(seqNum, ruid, tardy, "Particle tardy")
  }

  override def particleCancelled(seqNum: FabricWaveSeqNum, ruid: VitalsUid): VitalsUid = {
    particleUpdate(seqNum, ruid, cancelled, "Particle cancelled")
  }

  override def particleSucceeded(seqNum: FabricWaveSeqNum, ruid: VitalsUid): VitalsUid = {
    particleUpdate(seqNum, ruid, succeeded, "Particle succeeded")
  }

  override def particleFailed(seqNum: FabricWaveSeqNum, ruid: VitalsUid, reason: String): VitalsUid = {
    particleUpdate(seqNum, ruid, failed, reason)
  }

  override def clear(): Unit = this.synchronized {
    requests.clear()
    requestKeys.clear()
    waves.clear()
    _lastReset = System.currentTimeMillis()
  }

  override def get(guid: VitalsUid): Option[RequestState] = this.synchronized {
    requests.get(findKey(guid)) match {
      case null => None
      case req => Some(req)
    }
  }

  def foreach[T](work: RequestState => T): Unit = {
    val c = new function.Consumer[RequestState] {
      override def accept(req: RequestState): Unit = work(req)
    }
    requests.values().stream().forEach(c)
  }
}
