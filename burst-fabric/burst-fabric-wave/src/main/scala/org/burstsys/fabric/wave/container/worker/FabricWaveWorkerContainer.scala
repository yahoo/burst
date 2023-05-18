/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.container.worker

import org.burstsys.fabric.container.worker.{FabricWorkerContainer, FabricWorkerContainerContext}
import org.burstsys.fabric.wave.data.model.generation.FabricGeneration
import org.burstsys.fabric.wave.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.wave.data.worker.FabricWorkerData
import org.burstsys.fabric.wave.data.worker.cache.FabricSnapCache
import org.burstsys.fabric.wave.data.worker.store.{startWorkerStores, stopWorkerStores}
import org.burstsys.fabric.wave.exception.{FabricException, FabricGenericException}
import org.burstsys.fabric.wave.execution.model.pipeline.{FabricPipelineEvent, FabricPipelineEventListener, addPipelineSubscriber}
import org.burstsys.fabric.wave.execution.worker.FabricWorkerEngine
import org.burstsys.fabric.wave.execution.{FabricExecutionEvent, FabricExecutionException, FabricLoadEvent}
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.{FabricNetworkConfig, message}
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg
import org.burstsys.fabric.wave.message.cache.{FabricNetCacheOperationReqMsg, FabricNetCacheOperationRespMsg, FabricNetSliceFetchReqMsg, FabricNetSliceFetchRespMsg}
import org.burstsys.fabric.wave.message.scatter.FabricNetProgressMsg
import org.burstsys.fabric.wave.message.wave.{FabricNetParticleReqMsg, FabricNetParticleRespMsg}
import org.burstsys.fabric.wave.message.{FabricNetCacheOperationReqMsgType, FabricNetParticleReqMsgType, FabricNetSliceFetchReqMsgType}
import org.burstsys.fabric.wave.trek.FabricWorkerRequestTrekMark
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success}

/**
 * the one per JVM top level container for a Fabric Worker
 */
trait FabricWaveWorkerContainer extends FabricWorkerContainer[FabricWaveWorkerListener] {

  /**
   * data management on the worker side
   */
  def data: FabricWorkerData

  /**
   * the worker execution engine
   */
  def engine: FabricWorkerEngine

  /**
   * set the cache to direct messages to
   */
  def withCache(cache: FabricSnapCache): this.type

  /**
   * cache to direct messages to
   */
  def cache: FabricSnapCache
}

abstract class
FabricWaveWorkerContainerContext(netConfig: FabricNetworkConfig)
  extends FabricWorkerContainerContext[FabricWaveWorkerListener](netConfig) with FabricWaveWorkerContainer with FabricPipelineEventListener {

  override def serviceName: String = s"fabric-worker-container"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////

  private[this]
  val _data: FabricWorkerData = FabricWorkerData(this)

  private[this]
  val _engine: FabricWorkerEngine = FabricWorkerEngine(this)

  private[this]
  var _cache: FabricSnapCache = _

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def data: FabricWorkerData = _data

  final override
  def engine: FabricWorkerEngine = _engine

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    try {
      synchronized {
        ensureNotRunning
        // start generic container
        super.start

        // start up local data service
        _data.start

        // start up local execution engine
        _engine.start

        // pipeline
        addPipelineSubscriber(this)

        // start all stores in classpath
        startWorkerStores(this)

        markRunning
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning

      // start all stores in classpath
      stopWorkerStores(this)

      _engine.stop
      _data.stop

      // stop generic container
      super.stop

      markNotRunning
    }
    this
  }

  override def withCache(cache: FabricSnapCache): this.type = {
    _cache = cache
    this
  }

  override def cache: FabricSnapCache = _cache

  // messaging
  override def onNetMessage(connection: FabricNetClientConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    messageId match {
      /////////////////// PARTICLES /////////////////
      case FabricNetParticleReqMsgType =>
        val msg = FabricNetParticleReqMsg(buffer)
        log debug s"FabricWaveWorkerContainer.onNetClientParticleReqMsg $msg"
        _listener.foreach(_.onNetClientParticleReqMsg(connection, msg))
        executeParticle(connection, msg)

      /////////////////// CACHE OPERATIONS /////////////////
      case FabricNetCacheOperationReqMsgType =>
        val msg = FabricNetCacheOperationReqMsg(buffer)
        _listener.foreach(_.onNetClientCacheOperationReqMsg(connection, msg))
        cacheManageOperation(connection, msg)

      /////////////////// CACHE SLICE FETCH /////////////////
      case FabricNetSliceFetchReqMsgType =>
        val msg = FabricNetSliceFetchReqMsg(buffer)
        _listener.foreach(_.onNetClientSliceFetchReqMsg(connection, msg))
        cacheSliceFetch(connection, msg)

      case mt =>
        log warn burstStdMsg(s"Unknown message type $mt")
        throw VitalsException(s"Worker receieved unknown message mt=$mt")
    }
  }

  override
  def onNetClientAssessReqMsg(connection: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = {
    _listener.foreach(_.onNetClientAssessReqMsg(connection, msg))
  }

  // Cache
  /**
   * Respond to a request from the supervisor to perform a cache operation.
   */
  final
  def cacheManageOperation(connection: FabricNetClientConnection, msg: FabricNetCacheOperationReqMsg): Unit = {
    val resultPromise = Promise[Unit]()

    def sendResponse(generations: Array[FabricGeneration]): Future[Unit] = {
      val responsePromise = Promise[Unit]()
      connection.transmitControlMessage(FabricNetCacheOperationRespMsg(msg, connection.clientKey, connection.serverKey, generations)) onComplete {
        case Failure(t) => responsePromise.failure(t)
        case Success(_) => responsePromise.success((): Unit)
      }
      responsePromise.future
    }

    if (cache == null) {
      sendResponse(Array.empty) onComplete {
        case Failure(t) => resultPromise.failure(t)
        case Success(()) => resultPromise.success((): Unit)
      }
    } else {
      cache.cacheGenerationOp(guid = msg.guid, operation = msg.operation, generationKey = msg.generationKey, parameters = None) onComplete {
        case Failure(t) => resultPromise.failure(t)
        case Success(g) => sendResponse(g.toArray) onComplete {
          case Failure(t) => resultPromise.failure(t)
          case Success(_) => resultPromise.success((): Unit)
        }
      }
    }
    Await.result(resultPromise.future, 10.minutes)
  }

  /**
   * Respond to a request from the supervisor requesting slices for a particular generation key
   */
  final
  def cacheSliceFetch(connection: FabricNetClientConnection, msg: FabricNetSliceFetchReqMsg): Unit = {
    val resultPromise = Promise[Unit]()

    def sendResponse(slices: Array[FabricSliceMetadata]): Future[Unit] = {
      val responsePromise = Promise[Unit]()
      connection.transmitControlMessage(FabricNetSliceFetchRespMsg(msg, connection.clientKey, connection.serverKey, slices)) onComplete {
        case Failure(t) => responsePromise.failure(t)
        case Success(_) => responsePromise.success((): Unit)
      }
      responsePromise.future
    }

    if (cache == null) {
      sendResponse(Array.empty) onComplete {
        case Failure(t) => resultPromise.failure(t)
        case Success(_) => resultPromise.success((): Unit)
      }
    } else {
      cache.cacheSliceOp(guid = msg.guid, generationKey = msg.generationKey) onComplete {
        case Failure(t) => resultPromise.failure(t)
        case Success(g) => sendResponse(g.toArray) onComplete {
          case Failure(t) => resultPromise.failure(t)
          case Success(_) => resultPromise.success((): Unit)
        }
      }
    }
    Await.result(resultPromise.future, 10.minutes)
  }

  // Particles
  private[this]
  val _guidToRuid: ConcurrentHashMap[VitalsUid, VitalsUid] = new ConcurrentHashMap[VitalsUid, VitalsUid]()

  private[this]
  val _reportingGuids = ConcurrentHashMap.newKeySet[VitalsUid]()

  override def onEvent: PartialFunction[FabricPipelineEvent, Boolean] = {
    case e: FabricLoadEvent =>
      val ruid = _guidToRuid.get(e.guid)
      if (ruid == null)
        log debug s"no ruid for ${e.guid}"
      else {
        if (_reportingGuids.contains(e.guid))
          _netClient.connection.transmitControlMessage(FabricNetProgressMsg(_netClient.connection.clientKey, _netClient.connection.serverKey, e.guid, ruid, e.eventId, e.nanos, e.store, e.event))
      }
      true

    case e: FabricExecutionEvent =>
      val ruid = _guidToRuid.get(e.guid)
      if (ruid == null)
        log debug s"no ruid for ${e.guid}"
      else {
        if (_reportingGuids.contains(e.guid))
          _netClient.connection.transmitControlMessage(FabricNetProgressMsg(_netClient.connection.clientKey, _netClient.connection.serverKey, e.guid, ruid, e.eventId, e.nanos))
      }
      true
  }

  /**
   * local client-worker has an incoming particle request
   */
  final
  def executeParticle(connection: FabricNetClientConnection, msg: FabricNetParticleReqMsg): Unit = {
    val guid = msg.particle.slice.guid
    val ruid = msg.ruid
    val tag = s"FabricWaveWorkerContainer.executeParticle(guid=$guid, ruid=$ruid)"
    val span = FabricWorkerRequestTrekMark.begin(guid)
    try {
      _guidToRuid.put(guid, ruid)
      if (msg.particle.instrumented)
        _reportingGuids.add(guid)
      if (engine == null) throw FabricExecutionException(s"$tag no engine configured for worker")
      log debug s"$tag executing particle ${msg.particle.slice.datasource} slice=${msg.particle.slice.sliceKey}"
      val gather = engine.executionParticleOp(ruid, msg.particle)
      // TODO - where do we compress these...
      connection.transmitDataMessage(FabricNetParticleRespMsg(msg, connection.clientKey, connection.serverKey, gather)) onComplete {
        case Success(_) =>
          FabricWorkerRequestTrekMark.end(span)
        case Failure(_) =>
          FabricWorkerRequestTrekMark.fail(span)
      }

    } catch safely {

      // transmit back fabric related exception for supervisor to sort out
      case t: FabricException =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        FabricWorkerRequestTrekMark.fail(span)
        connection.transmitControlMessage(FabricNetParticleRespMsg(msg, connection.clientKey, connection.serverKey, t))

      // something unpredictable happened...
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        FabricWorkerRequestTrekMark.fail(span)
        connection.transmitControlMessage(FabricNetParticleRespMsg(msg, connection.clientKey, connection.serverKey, FabricGenericException(t)))

    } finally {
      _guidToRuid.remove(guid)
      _reportingGuids.remove(guid)
    }
  }

}
