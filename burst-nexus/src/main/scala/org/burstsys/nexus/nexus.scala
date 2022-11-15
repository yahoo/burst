/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import io.netty.channel.Channel
import org.burstsys.nexus.client.NexusClient
import org.burstsys.nexus.client.NexusClientReporter
import org.burstsys.nexus.server.NexusServer
import org.burstsys.nexus.server.NexusServerReporter
import org.burstsys.nexus.transmitter.NexusTransmitter
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net._
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.VitalsReporterSource

import java.net.InetSocketAddress
import java.util.UUID
import scala.concurrent.duration._
import scala.language.postfixOps


/**
 * [[http://netty.io/wiki/new-and-noteworthy-in-4.0.html]]
 * [[http://netty.io/wiki/new-and-noteworthy-in-4.1.html]]
 * [[https://github.com/netty/netty/wiki/Reference-counted-objects]]
 */
package object nexus extends VitalsReporterSource with VitalsLogger {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // VitalsReporterSource
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reporters: Array[VitalsReporter] = Array(
    NexusServerReporter,
    NexusClientReporter,
    NexusReporter
  )

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NexusConfig
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final case
  class NexusConfig(
                     isServer: Boolean,
                     msTimeout: Long = 1000L, maxWaits: Long = 10, maxBytesBetweenFlush: Long = 50e3.toLong,
                     maxPacketsBetweenFlush: Int = 50, maxNsBetweenFlush: Long = (100 millis).toNanos,
                     lowWaterMark: Int = 10, highWaterMark: Int = 20) {

    def printRole: String = if (isServer) "role=server" else "role=client"

  }

  def port: VitalsHostPort = nexus.configuration.burstNexusServerPortProperty.get

  /**
   * A timeout during any aspect (control or data plane) within a nexus transaction.
   *
   * @param s
   */
  final case class NexusTimeoutException(s: String) extends RuntimeException(s)

  ///////////////////////////////////////////////////////////////////////////
  // Io Mode
  ///////////////////////////////////////////////////////////////////////////

  /**
   * NOTE THAT TO USE ``EPollIoMode`` you need to install native support
   * https://netty.io/wiki/native-transports.html
   */
  object NexusIoMode extends Enumeration {

    type NexusIoMode = Value

    val EPollIoMode, NioIoMode, KqIoMode = Value

  }

  ////////////////////////////////////////////////////////////////////////////////////
  // accessors
  ////////////////////////////////////////////////////////////////////////////////////

  /**
   * Each Nexus messages RPC has a __unique within the number space of an integer__
   * request/response identifier.
   */
  type NexusRequestUid = Int

  /**
   * This is the general UID case that can be used either as a [[NexusGlobalUid]]
   * or a [[NexusStreamUid]]
   */
  type NexusUid = String

  /**
   * This is the macro level identifier for all client/server stream exchanges
   * across a complete parallel nexus inter-cluster data transfer
   */
  type NexusGlobalUid = String

  /**
   * This is a unique per stream identifier for a single client/server exchange
   * as a part of a complete parallel nexus inter-cluster data transfer
   */
  type NexusStreamUid = String

  /**
   * FabricSliceKey in the context of NexusStream
   */
  type NexusSliceKey = Long

  /**
   * This is a UID for both the macro level data transfer and the individual streams
   * i.e. either a [[NexusGlobalUid]] or a [[NexusStreamUid]]
   *
   * @return
   */
  final
  def newNexusUid: NexusUid =
    s"NX${UUID.randomUUID().toString.toUpperCase.replaceAll("-", "")}"

  ///////////////////////////////////////////////////////////////////////////
  // Public API
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Get a Nexus Server to use...
   */
  def grabServer(ipAddress: VitalsHostAddress): NexusServer = {
    NexusServer(serverHost = ipAddress, config = server.serverConfig).start
  }

  /**
   * Release a Nexus server. Not sure how useful this is for now...
   */
  def releaseServer(server: NexusServer): Unit = {
    server.stop
  }

  /**
   * Get a Nexus Client to use
   *
   * @param ipAddress
   * @param port
   * @return
   */
  def grabClientFromPool(ipAddress: VitalsHostAddress, port: Int): NexusClient =
    nexus.client.grabClientFromPool(serverHost = ipAddress, serverPort = port)

  /**
   * Return a next client to the pool
   *
   * @param client
   */
  def releaseClientToPool(client: NexusClient): Unit = {
    nexus.client.releaseClientToPool(client)
  }

  /**
   * Each connection between ''server'' and ''client'' has a 1:1 pair of connection objects, one
   * on the server [[org.burstsys.nexus.server.connection.NexusServerConnection]] and one on the client
   * [[org.burstsys.nexus.client.connection.NexusClientConnection]].
   * Each manages the state associated with that connection including the protocols associated
   * with the control and data plane communication. A given client
   * can have only one connection where as a server can have as many as their are connected clients.
   */
  trait NexusConnection extends AnyRef with NexusChannel {

    /**
     * The transmitter on this connection
     *
     * @return
     */
    def transmitter: NexusTransmitter

  }

  /**
   * useful stuff for netty channels
   */
  trait NexusChannel extends Any {

    /**
     * The NETTY channel associated with this connection
     *
     * @return
     */
    def channel: Channel

    /**
     * remote address for this connection
     *
     * @return
     */
    final
    def remoteAddress: VitalsHostName = {
      if (channel.remoteAddress == null) "unknown"
      else channel.remoteAddress.asInstanceOf[InetSocketAddress].getAddress.getHostAddress
    }

    /**
     * remote port for this connection
     *
     * @return
     */
    final
    def remotePort: VitalsHostPort = {
      if (channel.remoteAddress == null) -1
      else channel.remoteAddress.asInstanceOf[InetSocketAddress].getPort
    }

    /**
     * local address for this connection
     *
     * @return
     */
    final
    def localAddress: VitalsHostName = {
      if (channel.remoteAddress == null) "unknown"
      else channel.localAddress.asInstanceOf[InetSocketAddress].getAddress.getHostAddress
    }

    /**
     * local address for this connection
     *
     * @return
     */
    final
    def localPort: VitalsHostPort = {
      if (channel.remoteAddress == null) -1
      else channel.localAddress.asInstanceOf[InetSocketAddress].getPort
    }

    /**
     * @return a string showing the local and remote ends of the connection
     */
    final def link: String = s"local=$localAddress:$localPort, remote=$remoteAddress:$remotePort"

  }


}
