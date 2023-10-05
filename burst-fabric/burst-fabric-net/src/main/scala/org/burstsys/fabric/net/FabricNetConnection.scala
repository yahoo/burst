/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import io.netty.buffer.ByteBuf
import org.burstsys.fabric.topology.model.node.FabricNode
import io.netty.channel.Channel
import org.burstsys.fabric.net.message.FabricNetMsgType
import org.burstsys.fabric.net.transceiver.FabricNetTransmitter

/**
  * Each connection between ''server'' and ''client'' has a 1:1 pair of connection objects, one
  * on the server [[org.burstsys.fabric.net.server.connection.FabricNetServerConnection]] and one on the client
  * [[org.burstsys.fabric.net.client.connection.FabricNetClientConnection]].
  * Each manages the state associated with that connection including the protocols associated
  * with the control and data plane communication. A given client
  * can have only one connection where as a server can have as many as their are connected clients.
  */
trait FabricNetConnection extends AnyRef with FabricNetLink {

  /**
    * The NETTY channel associated with this connection
    *
    * @return
    */
  def channel: Channel

  /**
    * The transmitter on this connection
    *
    * @return
    */
  def transmitter: FabricNetTransmitter

  /**
    * Host Key for the client
    *
    * @return
    */
  def clientKey: FabricNode

  /**
    * Host key for the server
    *
    * @return
    */
  def serverKey: FabricNode

  /**
   * Process the connection message
   */
  def onMessage(messageId: FabricNetMsgType, buffer: Array[Byte]): Unit

  /**
   * Connection disconnect
   */
  def onDisconnect(): Unit

}

