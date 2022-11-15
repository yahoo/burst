/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client

import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.NexusIoMode._
import org.burstsys.nexus.client.connection.NexusClientConnection
import org.burstsys.nexus.configuration._
import org.burstsys.nexus.message.{NexusInboundFrameDecoder, NexusOutboundFrameEncoder}
import org.burstsys.nexus.receiver._
import org.burstsys.nexus.server.NexusServerReporter
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.transmitter.NexusTransmitter
import org.burstsys.nexus.{NexusConfig, NexusSliceKey, NexusStreamUid, NexusIoMode => _}
import org.burstsys.tesla.parcel.TeslaParcelStatus
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.configuration.SslGlobalProperties
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.net.ssl.{BurstKeyManagerFactory, BurstTrustManagerFactory}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort}
import org.burstsys.vitals.properties._
import org.burstsys.vitals.uid._
import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollSocketChannel}
import io.netty.channel.kqueue.{KQueueEventLoopGroup, KQueueSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.ssl.{SslContext, SslContextBuilder}

import scala.concurrent.duration.Duration._
import scala.concurrent.duration._
import org.burstsys.vitals.logging._

/**
  * A Nexus Client (stream data ''consumer'' side) in the protocol
  */
trait NexusClient extends VitalsService {

  /**
    * internal id for troubleshooting
    *
    * @return
    */
  def clientId: Int

  /**
    * The ip address of the remote nexus server
    *
    * @return
    */
  def serverHost: VitalsHostAddress

  /**
    * The ip port of the remote nexus server
    *
    * @return
    */
  def serverPort: VitalsHostPort

  /**
    * add a listener for nexus client events
    * @param listener
    * @return
    */
  def talksTo(listener: NexusClientListener): this.type

  /**
    * start a stream with the server associated with this client endpoint.
    *
    * @param guid a unique identifier for the request (load/query operation)
    * @param suid a unique identifier for this stream
    * @param properties
    * @param schema the schema for the data to be loaded
    * @param filter a filter to apply to the data when loading
    * @param pipe
    * @param sliceKey the slice information for this stream
    * @param clientHostname the hostname for this machine
    * @param serverHostname the hostname for the server
    * @return the stream that is started
    */
  def startStream(guid: VitalsUid, suid: NexusStreamUid, properties: VitalsPropertyMap, schema: BrioSchemaName, filter: BurstMotifFilter,
                  pipe: TeslaParcelPipe, sliceKey: NexusSliceKey, clientHostname: VitalsHostName, serverHostname: VitalsHostName): NexusStream

  /**
    * abort the current stream (if any) on this client with a specific [[TeslaParcelStatus]]
    */
  def abortStream(status: TeslaParcelStatus): Unit

  /**
    * the underlying NETTY channel
    */
  def nettyChannel: Channel

  /**
    * is the Channel still connected to the server
    */
  def isConnected: Boolean

  /**
    * A callback to notify the client that the connection to the server was disconnected
    */
  def channelDisconnected(): Unit

  /**
    * Is the Channel still being used
    */
  def isActive: Boolean

}

object NexusClient {

  final val idGenerator = new AtomicInteger()

  def apply(serverHost: VitalsHostAddress, serverPort: VitalsHostPort, config: NexusConfig = clientConfig): NexusClient =
    NexusClientContext(
      clientId = idGenerator.getAndIncrement, serverHost = serverHost: VitalsHostAddress,
      serverPort = serverPort: VitalsHostPort, config = config: NexusConfig
    )

}

private[client] final case
class NexusClientContext(clientId: Int, serverHost: VitalsHostAddress,
                         serverPort: VitalsHostPort,
                         config: NexusConfig, timeout: Duration = Inf,
                         ioMode: NexusIoMode = NioIoMode) extends NexusClient with NexusClientNetty with SslGlobalProperties {

  override def toString: String = serviceName

  override def serviceName: String = s"nexus-client(#$clientId, SSL=${burstNexusSslEnableProperty.get} $serverHost:$serverPort)"

  val modality: VitalsServiceModality = VitalsPojo

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _nettyChannel: Channel = _

  private[this]
  var _sslContext: Option[SslContext] = None

  private[this]
  var _transportClass: Class[_ <: Channel] = _

  private[this]
  val _socketAddress = new InetSocketAddress(serverHost, serverPort)

  private[this]
  var _eventLoopGroup: EventLoopGroup = _

  private[this]
  var _isActive: Boolean = false

  private[this]
  var _connection: NexusClientConnection = _

  private[this]
  var _listener: NexusClientListener = _

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override def nettyChannel: Channel = _nettyChannel

  override def isActive: Boolean = _connection != null && _connection.isActive

  override def isConnected: Boolean = _nettyChannel != null && _nettyChannel.isRegistered && _nettyChannel.isActive

  override
  def talksTo(listener: NexusClientListener): this.type = {
    _listener = listener
    if (_connection != null)
      _connection talksTo listener
    this
  }

  override
  def startStream(guid: VitalsUid, suid: NexusStreamUid, properties: VitalsPropertyMap, schema: BrioSchemaName, filter: BurstMotifFilter,
                  pipe: TeslaParcelPipe, sliceKey: NexusSliceKey, clientHostname: VitalsHostName, serverHostname: VitalsHostName): NexusStream = {
    _connection.startStream(guid: VitalsUid, suid: NexusStreamUid, properties: VitalsPropertyMap, schema: BrioSchemaName, filter: BurstMotifFilter,
      pipe: TeslaParcelPipe, sliceKey: NexusSliceKey, clientHostname: VitalsHostName, serverHostname: VitalsHostName)
  }

  override
  def abortStream(status: TeslaParcelStatus): Unit = {
    _connection.abortStream(status)
  }

  override def channelDisconnected(): Unit = {
    stopIfNotAlreadyStopped
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Pipeline
  ////////////////////////////////////////////////////////////////////////////////////

  def initializer: ChannelInitializer[SocketChannel] = new ChannelInitializer[SocketChannel]() {

    protected def initChannel(nettyChannel: SocketChannel): Unit = {
      val pipeline = nettyChannel.pipeline
      _nettyChannel = nettyChannel

      val transmitter = NexusTransmitter(clientId, isServer = false, channel = nettyChannel)
      _connection = NexusClientConnection(_nettyChannel, transmitter) talksTo _listener

      if (_sslContext.isDefined) {
        pipeline.addLast("client-inbound-stage-0", _sslContext.get.newHandler(nettyChannel.alloc, serverHost, serverPort))
      }

      // inbound goes in forward pipeline order
      pipeline.addLast("client-inbound-stage-1", NexusInboundFrameDecoder())
      pipeline.addLast("client-inbound-stage-2",
        NexusReceiver(clientId, isServer = false, transmitter = transmitter, clientListener = _connection, disconnectCallback = channelDisconnected)
      )

      // outbound goes in reverse pipeline order
      pipeline.addLast("client-outbound-stage-2", NexusOutboundFrameEncoder())
      pipeline.addLast("client-outbound-stage-1", transmitter)
    }
  }

  private
  def setupIoMode(): Unit = {
    ioMode match {
      case EPollIoMode =>
        _eventLoopGroup = new EpollEventLoopGroup()
        _transportClass = classOf[EpollSocketChannel]

      case NioIoMode =>
        _eventLoopGroup = new NioEventLoopGroup()
        _transportClass = classOf[NioSocketChannel]

      case KqIoMode =>
        _eventLoopGroup = new KQueueEventLoopGroup()
        _transportClass = classOf[KQueueSocketChannel]

      case _ => ???
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    synchronized {
      log info startingMessage
      try {
        setupIoMode()
        _nettyChannel = connectToServer.channel()
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(s"$serviceName: could not connect", t)
          throw t
      }

      NexusClientReporter.onClientConnectionStart()
      markRunning
      this
    }
  }

  /**
    * connect to the server - we need to detect and recover from connection loss.
    * Detecting connection loss appears to happen in [[org.burstsys.nexus.receiver.NexusReceiver.channelInactive]]
    */
  private
  def connectToServer: ChannelFuture = {

    if (burstNexusSslEnableProperty.get) {
      val certificate = new File(certPath)
      val privateKey = new File(keyPath)
      val caChain = new File(caPath)

      val contextBuilder = SslContextBuilder.forClient.keyManager(BurstKeyManagerFactory(privateKey, certificate))

      if (enableCompositeTrust)
        contextBuilder.trustManager(BurstTrustManagerFactory(caChain))
      else
        contextBuilder.trustManager(caChain)

      _sslContext = Some(contextBuilder.build)
    }

    val bootstrap = new Bootstrap
    bootstrap.group(_eventLoopGroup).channel(_transportClass)
    setNettyOptions(bootstrap).handler(initializer)
    val channelFuture = bootstrap.connect(_socketAddress)

    if (!channelFuture.awaitUninterruptibly.isSuccess) {
      val cause = channelFuture.cause
      throw VitalsException(s"$serviceName failed startup (${cause.getLocalizedMessage})", cause)
    }

    channelFuture
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      log info stoppingMessage
      try
        _nettyChannel.close.syncUninterruptibly
      finally _eventLoopGroup.shutdownGracefully
      _eventLoopGroup = null
      _nettyChannel = null
      markNotRunning
      NexusClientReporter.onClientConnectionStop()
      this
    }
  }

}
