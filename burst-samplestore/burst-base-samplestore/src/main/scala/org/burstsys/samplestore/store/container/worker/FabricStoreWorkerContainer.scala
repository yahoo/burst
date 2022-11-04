/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.worker

import org.burstsys.fabric.container.WorkerLog4JPropertiesFileName
import org.burstsys.fabric.container.worker.{FabricWorkerContainer, FabricWorkerContainerContext}
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message
import org.burstsys.fabric.net.message.AccessParameters
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg
import org.burstsys.nexus
import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry
import org.burstsys.samplesource.nexus.SampleSourceNexusServer
import org.burstsys.samplestore.store.container.{NexusConnectedPortAssessParameterName, NexusHostAddrAssessParameterName, NexusHostNameAssessParameterName, NexusPortAssessParameterName}
import org.burstsys.samplestore.store.message.FabricStoreMetadataReqMsgType
import org.burstsys.samplestore.store.message.metadata.{FabricStoreMetadataReqMsg, FabricStoreMetadataRespMsg}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

/**
 * the one per JVM top level container for a Fabric Worker
 */
trait FabricStoreWorkerContainer extends FabricWorkerContainer[FabricStoreWorkerListener]


class
FabricStoreWorkerContainerContext extends FabricWorkerContainerContext[FabricStoreWorkerListener] with FabricStoreWorkerContainer {

  override def serviceName: String = s"store-worker-container"

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

        SampleSourceNexusServer.startIfNotAlreadyStarted

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

      SampleSourceNexusServer.stopIfNotAlreadyStopped

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
        log debug s"FabricStoreWorkerContainer.onNetClientParticleReqMsg $msg"
        _listener.foreach(_.onStoreMetadataReqMsg(connection, msg))
        val wkr = SampleSourceHandlerRegistry.getWorker(msg.sourceName)
        wkr.putBroadcastVars(msg.metadata)
        connection.transmitControlMessage(FabricStoreMetadataRespMsg(msg, msg.receiverKey, msg.senderKey, System.currentTimeMillis()))

      case mt =>
        log warn burstStdMsg(s"Unknown message type $mt")
        throw VitalsException(s"Worker receieved unknown message mt=$mt")
    }
  }

  override
  def onNetClientAssessReqMsg(connection: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = {
    _listener.foreach(_.onNetClientAssessReqMsg(connection, msg))
  }


  override def prepareAccessRespParameters(parameters: AccessParameters): AccessParameters = {
    var p = parameters

    p = p ++ Map(
      NexusHostAddrAssessParameterName -> SampleSourceNexusServer.nexusServer.nettyChannel.localAddress().toString.asInstanceOf[Serializable],
      NexusHostNameAssessParameterName -> SampleSourceNexusServer.nexusServer.serverHost.asInstanceOf[Serializable],
      NexusConnectedPortAssessParameterName -> SampleSourceNexusServer.nexusServer.serverPort.asInstanceOf[Serializable],
      NexusPortAssessParameterName -> nexus.port.asInstanceOf[Serializable]
    )

    _listener.foreach { l =>
      p = l.prepareAccessRespParameters(p)
    }
    p
  }

  override def log4JPropertiesFileName: String = WorkerLog4JPropertiesFileName
}
