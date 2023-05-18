/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http

import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.glassfish.grizzly.websockets.WebSocketEngine

import scala.collection.mutable

/**
  * The interface between Grizzly and Burst
  */
trait FabricWebSocketService extends VitalsService {

  /**
    * Add a new websocket application
    *
    * @param url the base path for the application
    */
  def open(url: String, listener: FabricWebSocketListener): FabricWebSocketGroup

}

object FabricWebSocketService {
  def apply(): FabricWebSocketService = WebSocketServiceContext(VitalsStandardServer)
}

private final case
class WebSocketServiceContext(modality: VitalsServiceModality) extends FabricWebSocketService {

  override def serviceName: String = s"fabric-websocket"

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Private State
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _sockets: mutable.HashMap[String, FabricWebSocketGroup] = new mutable.HashMap[String, FabricWebSocketGroup]

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def open(url: String, listener: FabricWebSocketListener): FabricWebSocketGroup = {
    val socket = FabricWebSocketGroup(url, listener)
    _sockets += url -> socket
    socket
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////
  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    WebSocketEngine.getEngine.unregisterAll()
    markNotRunning
    this
  }

}
