/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.burstsys.fabric.container.SupervisorLog4JPropertiesFileName
import org.burstsys.fabric.container.supervisor.{FabricSupervisorContainer, FabricSupervisorContainerContext, FabricSupervisorListener}
import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetTetherMsg}
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.net.{FabricNetworkConfig, message}
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.samplesource.handler.{SampleSourceHandlerRegistry, SimpleSampleStoreApiServerDelegate}
import org.burstsys.samplesource.service.MetadataParameters
import org.burstsys.samplesource.{SampleStoreTopology, SampleStoreTopologyProvider}
import org.burstsys.samplestore.api.BurstSampleStoreApiViewGenerator
import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreAPIListener
import org.burstsys.samplestore.api.SampleStoreDataLocus
import org.burstsys.samplestore.api.server.SampleStoreApiServer
import org.burstsys.samplestore.configuration.sampleStoreRestPort
import org.burstsys.samplestore.store.container._
import org.burstsys.samplestore.store.message.FabricStoreMetadataRespMsgType
import org.burstsys.samplestore.store.message.metadata.{FabricStoreMetadataReqMsg, FabricStoreMetadataRespMsg}
import org.burstsys.tesla
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.burstLocMsg
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.net.SimpleServerHandler
import org.burstsys.vitals.net.VitalsHostPort
import org.burstsys.vitals.properties.VitalsPropertyMap

import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.Future
import scala.language.postfixOps

/**
 * the one per JVM top level container for a Fabric Supervisor
 */
trait SampleStoreFabricSupervisorContainer
  extends FabricSupervisorContainer[SampleStoreFabricSupervisorListener]
    with SampleStoreFabricSupervisorAPI
    with SampleStoreTopologyProvider

class SampleStoreFabricSupervisorContainerContext(netConfig: FabricNetworkConfig, var storeListenerProperties: VitalsPropertyMap = Map.empty)
  extends FabricSupervisorContainerContext[SampleStoreFabricSupervisorListener](netConfig)
  with SampleStoreFabricSupervisorContainer
  with FabricTopologyListener
  with SampleStoreTopologyProvider
  with SampleStoreAPIListener {

  override def serviceName: String = s"fabric-store-supervisor-container"

  private val thriftApiServer = SampleStoreApiServer(SimpleSampleStoreApiServerDelegate(this, storeListenerProperties))

  private val _server = HttpServer.create()

  private val _requests = new ConcurrentLinkedQueue[ViewRequest]()

  private lazy val _netAddress: InetSocketAddress = new InetSocketAddress(sampleStoreRestPort.asOption.getOrElse(0))

  private final val TCP_BACKLOG = 0 // use system default

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

      SampleSourceHandlerRegistry.startIfNotAlreadyStarted

      // start fabric container
      super.start

      // start JSON dash
      log info s"Starting Rest server on port ${_netAddress}"
      _server.bind(_netAddress, TCP_BACKLOG)
      _server.createContext("/", new SimpleServerHandler() {
         override def doGet(path: String, exchange: HttpExchange): Unit = {
           case class StoreInfo(name: String) {
             val vars: MetadataParameters = SampleSourceHandlerRegistry.getSupervisor(name).getBroadcastVars
           }

           val response = Map[String, Any](
             ("workers", topology.allWorkers.map(_.forExport)),
             ("requests", _requests),
             ("stores", SampleSourceHandlerRegistry.getSources.map(StoreInfo)),
           )
           writeJSON(exchange, mapper.writeValueAsString(response))
        }
      })
      _server.setExecutor(tesla.thread.request.teslaRequestExecutorService)
      _server.start()

      // monitor topology changes
      topology.talksTo(this)

      thriftApiServer.talksTo(this).start


      markRunning
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning

      thriftApiServer.stop
      _server.stop(1)

      super.stop

      markNotRunning
    }
    this
  }

  ////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////

  override def updateMetadata(connection: FabricNetServerConnection, sourceName: String, metadata: MetadataParameters): Future[Unit] = {
    connection.transmitDataMessage(FabricStoreMetadataReqMsg(connection.serverKey, connection.clientKey, sourceName, metadata))
  }

  override def updateMetadata(sourceName: String): Future[Unit] = {
    val currentMetadata = SampleSourceHandlerRegistry.getSupervisor(sourceName).getBroadcastVars
    val updateFutures = topology.healthyWorkers.flatMap{worker =>
      topology.getWorker(worker).map{w =>
        updateMetadata(w.connection, sourceName, currentMetadata)
      }
    }
    Future.reduceLeft(updateFutures.toIndexedSeq){(_, _) => ()}
  }

  override def onNetMessage(connection: FabricNetServerConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    messageId match {
      /////////////////// Metadata /////////////////
      case FabricStoreMetadataRespMsgType =>
        val msg = FabricStoreMetadataRespMsg(buffer)
        log debug burstLocMsg(s"FabricStoreSupervisorContainer.onNetClientParticleReqMsg $msg")
        filteredForeach[SampleStoreFabricSupervisorListener](_.onStoreMetadataRespMsg(connection, msg))

      case mt =>
        log warn burstLocMsg(s"Unknown message type $mt")
        throw VitalsException(s"Supervisor receieved unknown message mt=$mt")
    }
  }

  override def onDisconnect(connection: FabricNetServerConnection): Unit = {
    filteredForeach[FabricSupervisorListener](_.onDisconnect(connection))
  }

  override def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetTetherMsg): Unit = {
    filteredForeach[FabricSupervisorListener](_.onNetServerTetherMsg(connection,msg))
  }

  override def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {
    filteredForeach[FabricSupervisorListener](_.onNetServerAssessRespMsg(connection,msg))
  }

  override def onTopologyWorkerGained(worker: FabricTopologyWorker): Unit = {
    topology.getWorker(worker) match {
      case Some(w) =>
        // new worker gets broadcast of metadata for all known sources and source gets notified
        for (s <- SampleSourceHandlerRegistry.getSources) {
          val supervisor = SampleSourceHandlerRegistry.getSupervisor(s)
          log debug burstLocMsg(s"supervisor=${supervisor.name} worker=${w.nodeName}")
          supervisor.onSampleStoreDataLocusAdded(convertToLocus(worker))
          updateMetadata(w.connection, s, supervisor.getBroadcastVars)
        }
      case None =>
    }
  }


  override def getTopology: SampleStoreTopology = {
    SampleStoreTopology(topology.healthyWorkers.map(w => convertToLocus(w.asInstanceOf[FabricTopologyWorker])))
  }

  override def onTopologyWorkerLost(worker: FabricTopologyWorker): Unit = {
    // notified source supervisors of lots worker
    for (s <- SampleSourceHandlerRegistry.getSources) {
      val supervisor = SampleSourceHandlerRegistry.getSupervisor(s)
      log debug burstLocMsg(s"supervisor=${supervisor.name} worker=${worker.nodeName}")
      supervisor.onSampleStoreDataLocusRemoved(convertToLocus(worker))
    }
  }

  private def convertToLocus(worker: FabricTopologyWorker): SampleStoreDataLocus = {
    val nexusPort: VitalsHostPort = {
      if (worker.assessment != null && worker.assessment.parameters != null)
        worker.assessment.parameters(NexusPortAssessParameterName).asInstanceOf[VitalsHostPort]
      else
        -1
    }

    val partitionProperties: VitalsPropertyMap = Map()
    SampleStoreDataLocus(worker.workerProcessId.toString, worker.nodeAddress, worker.nodeName, nexusPort, partitionProperties)
  }

  override def log4JPropertiesFileName: String = SupervisorLog4JPropertiesFileName

  override def onViewGenerationRequest(guid: String, dataSource: BurstSampleStoreDataSource): Unit = {
    _requests.offer(ViewRequest(guid, dataSource))
    while (_requests.size > 10) {
      _requests.poll()
    }
  }

  override def onViewGeneration(guid: String, dataSource: BurstSampleStoreDataSource, result: BurstSampleStoreApiViewGenerator): Unit = {}
}

case class ViewRequest(guid: String, datasource: BurstSampleStoreDataSource, now: Long = System.currentTimeMillis)
