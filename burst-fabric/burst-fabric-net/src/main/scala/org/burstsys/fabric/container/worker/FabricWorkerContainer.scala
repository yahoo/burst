/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.worker

import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.FabricContainer
import org.burstsys.fabric.container.FabricContainerContext
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.client.{FabricNetClient, FabricNetClientListener}
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg
import org.burstsys.fabric.net.{FabricNetworkConfig, message}
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandaloneServer, VitalsStandardServer}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

/**
 * the one per JVM top level container for a Fabric Worker
 */
trait FabricWorkerContainer[T <: FabricWorkerListener] extends FabricContainer with FabricNetClientListener {
  /**
   * wire up the an event handler for this container
   */
  def talksTo(listener: T): this.type

}

abstract class
FabricWorkerContainerContext[T <: FabricWorkerListener](netConfig: FabricNetworkConfig)
  extends FabricContainerContext with FabricWorkerContainer[T] {
  override def serviceName: String = s"fabric-worker-container"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////

  final lazy
  val bootModality: VitalsServiceModality = if (configuration.burstFabricWorkerStandaloneProperty.get)
    VitalsStandaloneServer else VitalsStandardServer

  protected[this]
  val _netClient: FabricNetClient = FabricNetClient(this, netConfig)

  protected[this]
  var _listener: Option[T] = None

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def talksTo(listener: T): this.type = {
    assert(_listener.isEmpty)
    _listener = Some(listener)
    this
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    try {
      synchronized {
        ensureNotRunning

        if (containerId.isEmpty) {
          containerId = System.currentTimeMillis()
        }

        // start generic container
        super.start

        // fabric protocol client (worker side)
        _netClient.start

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
      _netClient.stop

      // stop generic container
      super.stop

      markNotRunning
    }
    this
  }

  // messaging
  override def onNetMessage(connection: FabricNetClientConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    messageId match {
      case mt =>
        log warn burstStdMsg(s"Unknown message type $mt")
        throw VitalsException(s"Worker receieved unknown message mt=$mt")
    }
  }

  override
  def onNetClientAssessReqMsg(connection: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = {
    _listener.foreach(_.onNetClientAssessReqMsg(connection, msg))
  }
}
