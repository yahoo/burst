/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application.websocket

import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandardServer}
import org.glassfish.grizzly.websockets.WebSocketEngine

import scala.collection.mutable

/**
  * The interface between Grizzly and Burst
  */
trait BurstWebSocketService extends VitalsService {

  /**
    * Add a new websocket application
    *
    * @param url the base path for the application
    */
  def open(url: String, listener: BurstDashWebSocketListener): BurstWebSocketGroup

}

object BurstWebSocketService {
  def apply(): BurstWebSocketService = WebSocketServiceContext(VitalsStandardServer)
}

private final case
class WebSocketServiceContext(modality: VitalsServiceModality) extends BurstWebSocketService {

  override def serviceName: String = s"dash-websocket"

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Private State
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _sockets: mutable.HashMap[String, BurstWebSocketGroup] = new mutable.HashMap[String, BurstWebSocketGroup]

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def open(url: String, listener: BurstDashWebSocketListener): BurstWebSocketGroup = {
    val socket = BurstWebSocketGroup(url, listener)
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
