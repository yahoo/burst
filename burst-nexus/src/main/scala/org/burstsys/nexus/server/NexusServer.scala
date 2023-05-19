/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.server

import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import org.burstsys.nexus.NexusIoMode._
import org.burstsys.nexus.configuration._
import org.burstsys.nexus.message.{NexusInboundFrameDecoder, NexusOutboundFrameEncoder}
import org.burstsys.nexus.receiver._
import org.burstsys.nexus.server.connection.NexusServerConnection
import org.burstsys.nexus.transmitter.NexusTransmitter
import org.burstsys.nexus.{NexusConfig, NexusIoMode => _}
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.configuration.SslGlobalProperties
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.net.ssl.{BurstKeyManagerFactory, BurstTrustManagerFactory}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort}
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollServerSocketChannel}
import io.netty.channel.kqueue.{KQueueEventLoopGroup, KQueueServerSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.{ServerSocketChannel, SocketChannel}
import io.netty.handler.ssl._
import org.burstsys.nexus

import scala.concurrent.duration.Duration
import scala.concurrent.duration.Duration._
import org.burstsys.vitals.logging._

import java.net.InetAddress

/**
  * The server (stream data provider) side of the next protocol
  * The ''server'' binds to a port given to it by the network stack. This port is then sent to clients. This is
  * ''not'' a ''well known'' port
  */
trait NexusServer extends VitalsService {

  /**
    * @return the config defined ''or'' dynamically bound port for this server (available once the server has started)
    */
  def serverPort: VitalsHostPort

  /**
    * @return the hostname/address for this how
    */
  def serverHost: VitalsHostName

  /**
    * the data feed for this server
    */
  def fedBy(feeder: NexusStreamFeeder): this.type

  /**
    * a listener for protocol events
    */
  def talksTo(listener: NexusServerListener): this.type

  /**
    * the associated NETTY channel
    */
  def nettyChannel: Channel

}

object NexusServer {

  final val idGenerator = new AtomicInteger()

  def apply(serverHost: VitalsHostAddress, config: NexusConfig = serverConfig): NexusServer =
    NexusServerContext(serverId = idGenerator.getAndIncrement, serverHost = serverHost: VitalsHostAddress, config = config: NexusConfig)
}

/**
  * The server (data provider) side of the next protocol
  * The ''server'' binds to a port given to it by the network stack. This port is then sent to clients. This is
  * ''not'' a ''well known'' port
  */
private[server] final case
class NexusServerContext(
                          serverId: Int,
                          serverHost: VitalsHostName,
                          config: NexusConfig,
                          timeout: Duration = Inf,
                          ioMode: NexusIoMode = NioIoMode
                        ) extends NexusServer with NexusServerNetty with SslGlobalProperties {
  override def toString: String = serviceName

  override def serviceName: String = s"nexus-server(#$serverId, SSL=${burstNexusSslEnableProperty.get} $serverHost:$serverPort)"

  val modality: VitalsServiceModality = VitalsPojo

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _nettyChannel: Channel = _

  private[this]
  var _socketAddress: InetSocketAddress = _

  private[this]
  var _sslContext: Option[SslContext] = None

  private[this]
  var _listenGroup: EventLoopGroup = _

  private[this]
  var _connectionGroup: EventLoopGroup = _

  private[this]
  var _transportClass: Class[_ <: ServerChannel] = _

  private[this]
  var _feeder: NexusStreamFeeder = _

  private[this]
  var _listener: NexusServerListener = _

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override def nettyChannel: Channel = _nettyChannel

  override
  def fedBy(feeder: NexusStreamFeeder): this.type = {
    _feeder = feeder
    this
  }

  override
  def talksTo(listener: NexusServerListener): this.type = {
    _listener = listener
    this
  }

  override
  def serverPort: VitalsHostPort = {
    if (_socketAddress == null) -1 else _socketAddress.getPort
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Pipeline
  ////////////////////////////////////////////////////////////////////////////////////

  private def initializer: ChannelInitializer[SocketChannel] = (nettyChannel: SocketChannel) => {

    val pipeline: ChannelPipeline = nettyChannel.pipeline

    val transmitter = NexusTransmitter(serverId, isServer = true, channel = nettyChannel, maxQueuedWrites = 1)
    val connection = NexusServerConnection(nettyChannel, transmitter, _feeder) talksTo _listener

    if (_sslContext.isDefined) {
      pipeline.addLast("server-inbound-stage-0", _sslContext.get.newHandler(nettyChannel.alloc))
    }

    // inbound goes in forward pipeline order
    pipeline.addLast("server-inbound-stage-1", NexusInboundFrameDecoder())
    pipeline.addLast("server-inbound-stage-2",
      NexusReceiver(serverId, isServer = true, transmitter, serverListener = connection)
    )

    // outbound goes in reverse pipeline order
    pipeline.addLast("server-outbound-stage-2", NexusOutboundFrameEncoder())
    pipeline.addLast("server-outbound-stage-1", transmitter)

  }

  private
  def setupIoMode(): Unit = {
    ioMode match {
      case EPollIoMode =>
        _listenGroup = new EpollEventLoopGroup()
        _connectionGroup = new EpollEventLoopGroup()
        _transportClass = classOf[EpollServerSocketChannel]


      case NioIoMode =>
        _listenGroup = new NioEventLoopGroup()
        _connectionGroup = new NioEventLoopGroup()
        _transportClass = classOf[NioServerSocketChannel]

      case KqIoMode =>
        _listenGroup = new KQueueEventLoopGroup()
        _connectionGroup = new KQueueEventLoopGroup()
        _transportClass = classOf[KQueueServerSocketChannel]

      case _ =>
        val e = VitalsException(s"Unsupported IO mode: $ioMode")
        log error(burstLocMsg(e), e)
        throw e
    }
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

        if (burstNexusSslEnableProperty.get) {
          val certificate = new File(certPath)
          val privateKey = new File(keyPath)
          val caChain = new File(caPath)

          val contextBuilder = SslContextBuilder.forServer(BurstKeyManagerFactory(privateKey, certificate))

          if (enableCompositeTrust)
            contextBuilder.trustManager(BurstTrustManagerFactory(caChain))
          else
            contextBuilder.trustManager(caChain)

          _sslContext = Some(contextBuilder.clientAuth(ClientAuth.REQUIRE).build)
        }

        val bootstrap = new ServerBootstrap()
          .group(_listenGroup, _connectionGroup)
          .channel(_transportClass)
        setNettyOptions(bootstrap).childHandler(initializer)
        val channelFuture = bootstrap.bind(new InetSocketAddress(null.asInstanceOf[InetAddress], nexus.port))

        if (!channelFuture.awaitUninterruptibly.isSuccess) {
          val cause = channelFuture.cause
          throw VitalsException(s"$serviceName failed startup (${cause.getLocalizedMessage})", cause)
        }


        _nettyChannel = channelFuture.channel()
        _socketAddress = _nettyChannel.asInstanceOf[ServerSocketChannel].localAddress()
      } catch safely {
        case t: Throwable =>
          log error(burstStdMsg(s"$serviceName: could not bind", t), t)
          throw t
      }

      markRunning
      NexusServerReporter.onServerConnectionStart()
      this
    }
  }

  def stop: this.type = {
    ensureRunning
    synchronized {
      log info stoppingMessage
      try
        _nettyChannel.close.syncUninterruptibly
      finally {
        _listenGroup.shutdownGracefully
        _connectionGroup.shutdownGracefully
      }
      _listenGroup = null
      _nettyChannel = null
      markNotRunning
      NexusServerReporter.onServerConnectionStop()
      this
    }
  }

}
