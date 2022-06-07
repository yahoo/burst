/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application.websocket

/**
 * Websocket contract
 */
trait BurstDashWebSocketListener extends Any {

  /**
   * Called when a browser connects a new websocket
   *
   * @param group  the websocket
   * @param socket the websocket that was opened
   */
  def onWebSocketOpen(group: BurstWebSocketGroup, socket: BurstWebSocket): Unit = {}

  /**
   * Called when a message is received from the browser
   *
   * @param group  the websocket
   * @param socket the websocket the message was received from
   * @param json   the message received
   */
  def onWebSocketReceive(group: BurstWebSocketGroup, socket: BurstWebSocket, json: Any): Unit = {}

  /**
   * Called when a message is received from the browser
   *
   * @param group   the websocket
   * @param socket  the websocket triggering the action
   * @param action  the action requested by the websocket
   * @param payload the full parsed payload
   */
  def onWebSocketAction(group: BurstWebSocketGroup, socket: BurstWebSocket, action: String, payload: Map[String, Any]): Unit = {}

  /**
   * Called when the browser closes the websocket
   *
   * @param group  the websocket
   * @param socket the websocket that closed
   */
  def onWebSocketClose(group: BurstWebSocketGroup, socket: BurstWebSocket): Unit = {}

}

