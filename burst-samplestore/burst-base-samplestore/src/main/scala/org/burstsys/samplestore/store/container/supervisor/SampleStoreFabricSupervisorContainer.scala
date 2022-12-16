/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor

import org.burstsys.fabric.container.SupervisorLog4JPropertiesFileName
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainerContext
import org.burstsys.fabric.container.supervisor.FabricSupervisorListener
import org.burstsys.fabric.net.message.assess.FabricNetAssessRespMsg
import org.burstsys.fabric.net.message.assess.FabricNetTetherMsg
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.net.FabricNetworkConfig
import org.burstsys.fabric.net.message
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry
import org.burstsys.samplesource.handler.SimpleSampleStoreApiServerDelegate
import org.burstsys.samplesource.service.MetadataParameters
import org.burstsys.samplesource.SampleStoreTopology
import org.burstsys.samplesource.SampleStoreTopologyProvider
import org.burstsys.samplestore.api.SampleStoreApiListener
import org.burstsys.samplestore.api.SampleStoreDataLocus
import org.burstsys.samplestore.api.server.SampleStoreApiServer
import org.burstsys.samplestore.store.container._
import org.burstsys.samplestore.store.container.supervisor.http.SampleStoreHttpBinder
import org.burstsys.samplestore.store.container.supervisor.http.endpoints.SampleStoreStatusEndpoint
import org.burstsys.samplestore.store.container.supervisor.http.services.ViewGenerationRequestLogService
import org.burstsys.samplestore.store.message.FabricStoreMetadataRespMsgType
import org.burstsys.samplestore.store.message.metadata.FabricStoreMetadataReqMsg
import org.burstsys.samplestore.store.message.metadata.FabricStoreMetadataRespMsg
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.burstLocMsg
import org.burstsys.vitals.net.VitalsHostPort
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.glassfish.hk2.utilities.binding.AbstractBinder

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
  with SampleStoreTopologyProvider {

  override def serviceName: String = s"fabric-store-supervisor-container"

  private val thriftApiServer = SampleStoreApiServer(SimpleSampleStoreApiServerDelegate(this, storeListenerProperties))

  private val requestLog = new ViewGenerationRequestLogService()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def start: this.type = {
    synchronized {
      ensureNotRunning

      if (containerId.isEmpty) {
        containerId = System.currentTimeMillis()
      }

      SampleSourceHandlerRegistry.startIfNotAlreadyStarted

      // start fabric container
      super.start

      // monitor topology changes
      topology.talksTo(this)

      thriftApiServer.talksTo(requestLog)
        .start


      markRunning
    }
    this
  }

  override def stop: this.type = {
    synchronized {
      ensureRunning

      thriftApiServer.stop

      super.stop

      markNotRunning
    }
    this
  }

  ////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////

  override def httpBinder: AbstractBinder = new SampleStoreHttpBinder(this, topology, requestLog, storeListenerProperties)


  override def httpResources: Array[Class[_]] = super.httpResources ++ Array(
    classOf[SampleStoreStatusEndpoint],
  )

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

}
