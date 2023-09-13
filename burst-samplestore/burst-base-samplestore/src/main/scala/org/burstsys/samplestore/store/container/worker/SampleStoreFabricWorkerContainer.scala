/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.worker

import org.burstsys.fabric.container.WorkerLog4JPropertiesFileName
import org.burstsys.fabric.container.worker.{FabricWorkerContainer, FabricWorkerContainerContext}
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message.AccessParameters
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg
import org.burstsys.fabric.net.{FabricNetworkConfig, message}
import org.burstsys.nexus
import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry
import org.burstsys.samplesource.nexus.SampleSourceNexusServer
import org.burstsys.samplestore.configuration.{sampleStoreNexusHostAddrOverride, sampleStoreNexusHostNameAddrOverride}
import org.burstsys.samplestore.store.container.{NexusConnectedPortAccessParameter, NexusHostAddrAccessParameter, NexusHostNameAccessParameter, NexusPortAccessParameter}
import org.burstsys.samplestore.store.message.FabricStoreMetadataReqMsgType
import org.burstsys.samplestore.store.message.metadata.{FabricStoreMetadataReqMsg, FabricStoreMetadataRespMsg}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.sysinfo.{SystemInfoComponent, SystemInfoService}

/**
 * the one per JVM top level container for a Fabric Worker
 */
trait SampleStoreFabricWorkerContainer extends FabricWorkerContainer[SampleStoreFabricWorkerListener]


class
SampleStoreFabricWorkerContainerContext(netConfig: FabricNetworkConfig)
  extends FabricWorkerContainerContext[SampleStoreFabricWorkerListener](netConfig)
    with SampleStoreFabricWorkerContainer
    with SystemInfoComponent {

  override def serviceName: String = s"samplestore-worker-container"

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

        // Look for sources
        SampleSourceHandlerRegistry.startIfNotAlreadyStarted
        SystemInfoService.registerComponent(this)

        SampleSourceNexusServer.startIfNotAlreadyStarted

        markRunning
      }
    } catch safely {
      case t: Throwable =>
        log error(burstStdMsg(t), t)
        throw t
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning

      SampleSourceNexusServer.stopIfNotAlreadyStopped
      SystemInfoService.deregisterComponent(this)

      // stop generic container
      super.stop

      markNotRunning
    }
    this
  }

  // messaging
  override def onNetMessage(connection: FabricNetClientConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    messageId match {
      /////////////////// Metadata /////////////////
      case FabricStoreMetadataReqMsgType =>
        val msg = FabricStoreMetadataReqMsg(buffer)
        log debug s"SampleStoreFabricWorkerContainer.onNetClientParticleReqMsg $msg"
        _listener.foreach(_.onStoreMetadataReqMsg(connection, msg))
        val wkr = SampleSourceHandlerRegistry.getWorker(msg.sourceName)
        wkr.putBroadcastVars(msg.metadata)
        connection.transmitControlMessage(FabricStoreMetadataRespMsg(msg, msg.receiverKey, msg.senderKey, System.currentTimeMillis()))

      case mt =>
        log warn burstStdMsg(s"Unknown message type $mt")
        throw VitalsException(s"Worker received unknown message mt=$mt")
    }
  }

  override
  def onNetClientAssessReqMsg(connection: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = {
    _listener.foreach(_.onNetClientAssessReqMsg(connection, msg))
  }


  override def prepareAccessParameters(parameters: AccessParameters): AccessParameters = {
    super.prepareAccessParameters(parameters) ++ Map(
      NexusHostAddrAccessParameter -> sampleStoreNexusHostAddrOverride.asOption
        .getOrElse(SampleSourceNexusServer.nexusServer.serverHost).asInstanceOf[Serializable],
      NexusHostNameAccessParameter -> sampleStoreNexusHostNameAddrOverride.asOption
        .getOrElse(SampleSourceNexusServer.nexusServer.serverHost).asInstanceOf[Serializable],
      NexusConnectedPortAccessParameter -> SampleSourceNexusServer.nexusServer.serverPort.asInstanceOf[Serializable],
      NexusPortAccessParameter -> nexus.port.asInstanceOf[Serializable]
    )
  }

  override def log4JPropertiesFileName: String = WorkerLog4JPropertiesFileName

  /**
   * @return name of component
   */
  override def name: String = serviceName

  /**
   * System info about component.
   *
   * @return Case classs that will be serialized to Json
   */
  override def status(level: Int, attributes: VitalsPropertyMap): AnyRef = {
    SampleSourceHandlerRegistry.getSources.toArray
  }
}
