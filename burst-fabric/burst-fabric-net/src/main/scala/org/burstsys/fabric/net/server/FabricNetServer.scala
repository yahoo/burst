/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollServerSocketChannel}
import io.netty.channel.kqueue.{KQueueEventLoopGroup, KQueueServerSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.net.FabricNetIoMode.FabricNetIoMode
import org.burstsys.fabric.net._
import org.burstsys.fabric.net.message.assess.{FabricNetAssessRespMsg, FabricNetHeartbeatMsg}
import org.burstsys.fabric.net.message.{FabricNetInboundFrameDecoder, FabricNetOutboundFrameEncoder}
import org.burstsys.fabric.net.receiver.FabricNetReceiver
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.net.transmitter.FabricNetTransmitter
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.healthcheck.VitalsHealthMonitoredService
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.Duration
import scala.concurrent.duration.Duration._
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.language.postfixOps

/**
 * The server (data provider) side of the next protocol
 * The ''server'' binds to a port given to it by the network stack. This port is then sent to clients. This is
 * ''not'' a ''well known'' port
 */
trait FabricNetServer extends FabricSupervisorService with FabricNetLink {

  def netConfig: FabricNetworkConfig

  def activeConnections: Array[FabricNetServerConnection]

}

object FabricNetServer {

  def apply(container: FabricSupervisorContainer[_], netConfig: FabricNetworkConfig): FabricNetServer =
    FabricNetServerContext(container, netConfig)
}

/**
 * The server (data provider) side of the next protocol
 * The ''server'' binds to a port given to it by the network stack. This port is then sent to clients. This is
 * ''not'' a ''well known'' port
 */
private[server] final case
class FabricNetServerContext(container: FabricSupervisorContainer[_], netConfig: FabricNetworkConfig) extends FabricNetServer
  with FabricNetServerNetty with FabricNetServerListener with VitalsHealthMonitoredService {

  override def toString: String = serviceName

  override def serviceName: String = s"fabric-net-server(containerId=${container.containerIdGetOrThrow}, ${netConfig.netSupervisorUrl})"

  override val modality: VitalsServiceModality = VitalsPojo

  ////////////////////////////////////////////////////////////////////////////////////
  // Config
  ////////////////////////////////////////////////////////////////////////////////////

  def ioMode: FabricNetIoMode = FabricNetIoMode.NioIoMode

  def timeout: Duration = Inf

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _serverChannel: Channel = _

  private[this]
  var _listenGroup: EventLoopGroup = _

  private[this]
  var _connectionGroup: EventLoopGroup = _

  private[this]
  var _transportClass: Class[_ <: ServerChannel] = _

  private[this]
  val _connections = new ConcurrentHashMap[(VitalsHostAddress, VitalsHostPort), FabricNetServerConnection]

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override def channel: Channel = _serverChannel

  ////////////////////////////////////////////////////////////////////////////////////
  // Pipeline
  ////////////////////////////////////////////////////////////////////////////////////


  private
  def initializer: ChannelInitializer[SocketChannel] = (channel: SocketChannel) => {

    val connectionPipeline = channel.pipeline

    val transmitter = FabricNetTransmitter(container, isServer = true, channel)
    val receiver = FabricNetReceiver(container, isServer = true, channel)
    val connection = FabricNetServerConnection(container, channel, transmitter, receiver)
    FabricNetReporter.recordConnectOpen()
    connection.talksTo(FabricNetServerContext.this)
    connection.start

    val clientAddress = connection.remoteAddress
    val clientPort = connection.remotePort
    _connections.put((clientAddress, clientPort), connection)
    log info burstStdMsg(s"NEW_CLIENT_CONNECTION $serviceName (now ${_connections.size} total) address=$clientAddress, port=$clientPort")

    // inbound goes in forward pipeline order
    connectionPipeline.addLast("server-inbound-stage-1", FabricNetInboundFrameDecoder())
    connectionPipeline.addLast("server-inbound-stage-2", receiver)

    // outbound goes in reverse pipeline order
    connectionPipeline.addLast("server-outbound-stage-2", FabricNetOutboundFrameEncoder())
    connectionPipeline.addLast("server-outbound-stage-1", transmitter)

  }

  private def setupIoMode(): Unit = {
    ioMode match {
      case FabricNetIoMode.EPollIoMode =>
        _listenGroup = new EpollEventLoopGroup(1) // single threaded listener
        // lots of threads for lots of connections
        _connectionGroup = new EpollEventLoopGroup(netConfig.maxConnections)
        _transportClass = classOf[EpollServerSocketChannel]

      case FabricNetIoMode.NioIoMode =>
        //        _listenGroup = new NioEventLoopGroup(1) // single threaded listener
        _listenGroup = new NioEventLoopGroup()
        // lots of threads for lots of connections
        //        _connectionGroup = new NioEventLoopGroup(netConfig.maxConnections)
        _connectionGroup = new NioEventLoopGroup()
        _transportClass = classOf[NioServerSocketChannel]

      case FabricNetIoMode.KqIoMode =>
        _listenGroup = new KQueueEventLoopGroup(1) // single threaded listener
        // lots of threads for lots of connections
        _connectionGroup = new KQueueEventLoopGroup(netConfig.maxConnections)
        _transportClass = classOf[KQueueServerSocketChannel]

      case _ =>
        val e =  VitalsException(s"unknown io mode $ioMode")
        log error(burstLocMsg(e), e)
        throw e
    }
    log debug burstStdMsg(s"fabric network server started in $ioMode with  $netConfig")
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////

  def start: this.type = {
    synchronized {
      ensureNotRunning
      log info startingMessage
      try {
        setupIoMode()

        val bootstrap = new ServerBootstrap
        bootstrap.group(_listenGroup, _connectionGroup).channel(_transportClass)
        setNettyOptions(bootstrap, netConfig).childHandler(initializer)
        val channelFuture = bootstrap.bind(netConfig.netSupervisorAddress, netConfig.netSupervisorPort)

        if (!channelFuture.awaitUninterruptibly.isSuccess) {
          val cause = channelFuture.cause
          val msg = s"$serviceName: server failed startup to ${netConfig.netSupervisorUrl}: ${cause.getLocalizedMessage}"
          log error burstStdMsg(msg)
          // log error getAllThreadsDump.mkString("\n")
          throw VitalsException(msg, cause)
        }

        _serverChannel = channelFuture.channel()
      } catch safely {
        case t: Throwable =>
          log error(burstStdMsg(s"$serviceName: could not bind", t), t)
          throw t
      }

      markRunning
      this
    }
  }

  def stop: this.type = {
    ensureRunning
    synchronized {
      log info stoppingMessage
      try {
        _connections.values.forEach(_.shutdown())
        _serverChannel.close.syncUninterruptibly
      } finally {
        _listenGroup.shutdownGracefully
        _connectionGroup.shutdownGracefully
      }
      _listenGroup = null
      _serverChannel = null
      markNotRunning
      this
    }
  }

  override def onNetMessage(connection:FabricNetServerConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    container.onNetMessage(connection, messageId, buffer)
  }

  override def onDisconnect(connection:FabricNetServerConnection): Unit ={
    _connections.remove((connection.remoteAddress, connection.remotePort))
    log debug burstStdMsg(s"removing connection ${connection.remoteAddress}:${connection.remotePort}")
    container.onDisconnect(connection)
  }

  override def onNetServerTetherMsg(connection: FabricNetServerConnection, msg: FabricNetHeartbeatMsg): Unit = {
    log debug burstStdMsg(s"tether for  connection ${connection.remoteAddress}:${connection.remotePort}")
    container.onNetServerTetherMsg(connection, msg)
  }

  override def onNetServerAssessRespMsg(connection: FabricNetServerConnection, msg: FabricNetAssessRespMsg): Unit = {
    log debug burstStdMsg(s"assess for  connection ${connection.remoteAddress}:${connection.remotePort}")
    container.onNetServerAssessRespMsg(connection, msg)
  }

  override def activeConnections: Array[FabricNetServerConnection] = {
    _connections.values().asScala.filter(_.isConnected).toArray
  }
}
