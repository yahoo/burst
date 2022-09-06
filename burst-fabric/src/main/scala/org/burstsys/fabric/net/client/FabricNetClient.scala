/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client

import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.{Timer, TimerTask}

import org.burstsys.fabric.container.FabricWorkerService
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.data.worker.cache.FabricSnapCache
import org.burstsys.fabric.execution.worker.FabricWorkerEngine
import org.burstsys.fabric.net.FabricNetIoMode.FabricNetIoMode
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message.{FabricNetInboundFrameDecoder, FabricNetOutboundFrameEncoder}
import org.burstsys.fabric.net.receiver.FabricNetReceiver
import org.burstsys.fabric.net.transmitter.FabricNetTransmitter
import org.burstsys.fabric.net.{FabricNetIoMode, FabricNetLink, FabricNetLocator, FabricNetworkConfig}
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.healthcheck.VitalsHealthMonitoredService
import org.burstsys.vitals.logging._
import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollSocketChannel}
import io.netty.channel.kqueue.{KQueueEventLoopGroup, KQueueSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

import scala.jdk.CollectionConverters._
import scala.concurrent.duration.Duration._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * a fabric network client (worker side)
 */
trait FabricNetClient extends FabricWorkerService with FabricNetLink with FabricNetLocator {

  /**
   * wire up a event handler for client events
   */
  def talksTo(listeners: FabricNetClientListener*): this.type

  /**
   * set the cache to direct messages to
   */
  def withCache(cache: FabricSnapCache): this.type

  /**
   * cache to direct messages to
   */
  def cache: FabricSnapCache

  /**
   * set the engine to direct messages to
   */
  def withEngine(engine: FabricWorkerEngine): this.type

  /**
   * engine to direct messages to
   */
  def engine: FabricWorkerEngine

  def connection: FabricNetClientConnection
}

object FabricNetClient {

  def apply(container: FabricWorkerContainer): FabricNetClient =
    FabricNetClientContext(container)

}

private[client] final case
class FabricNetClientContext(container: FabricWorkerContainer) extends FabricNetClient
  with FabricNetClientNetty with FabricNetLocator with VitalsHealthMonitoredService {

  override def toString: String = serviceName

  override def serviceName: String = s"fabric-net-client(containerId=${container.containerIdGetOrThrow}, $netMasterUrl)"

  val modality: VitalsServiceModality = VitalsPojo

  ////////////////////////////////////////////////////////////////////////////////////
  // Config
  ////////////////////////////////////////////////////////////////////////////////////

  def ioMode: FabricNetIoMode = FabricNetIoMode.NioIoMode

  def config: FabricNetworkConfig = clientConfig

  def timeout: Duration = Inf

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _clientChannel: Channel = _

  private[this]
  var _transportClass: Class[_ <: Channel] = _

  private[this]
  val _socketAddress: SocketAddress = netMasterSocketAddress

  private[this]
  var _eventLoopGroup: EventLoopGroup = _

  private[this]
  var _connection: FabricNetClientConnection = _

  private[this]
  val _listenerSet = ConcurrentHashMap.newKeySet[FabricNetClientListener].asScala

  private[this]
  var _engine: FabricWorkerEngine = _

  private[this]
  var _cache: FabricSnapCache = _

  private[this]
  val _stopping: AtomicBoolean = new AtomicBoolean(false)

  private[this]
  var _bootstrap: Bootstrap = _

  private[this]
  val _timer: Timer = new Timer()

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override def connection: FabricNetClientConnection = _connection

  override def channel: Channel = _clientChannel

  override def withCache(cache: FabricSnapCache): this.type = {
    _cache = cache
    this
  }

  override def cache: FabricSnapCache = _cache

  override def withEngine(engine: FabricWorkerEngine): this.type = {
    _engine = engine
    this
  }

  override def engine: FabricWorkerEngine = _engine

  override
  def talksTo(listeners: FabricNetClientListener*): this.type = {
    _listenerSet ++= listeners
    if (_connection != null)
      _connection talksTo (listeners: _*)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Pipeline
  ////////////////////////////////////////////////////////////////////////////////////

  def initializer: ChannelInitializer[SocketChannel] = new ChannelInitializer[SocketChannel]() {
    protected def initChannel(channel: SocketChannel): Unit = {
      _clientChannel = channel

      val clientPipeline = channel.pipeline

      val transmitter = FabricNetTransmitter(container, isServer = false, channel)
      val receiver = FabricNetReceiver(container, isServer = false, channel)
      _connection = FabricNetClientConnection(
        container, channel, transmitter, receiver, FabricNetClientContext.this
      ) talksTo (_listenerSet.toSeq: _*) start

      // inbound goes in forward pipeline order
      clientPipeline.addLast("client-inbound-stage-1", FabricNetInboundFrameDecoder())
      clientPipeline.addLast("client-inbound-stage-2", receiver)

      // outbound goes in reverse pipeline order
      clientPipeline.addLast("client-outbound-stage-2", FabricNetOutboundFrameEncoder())
      clientPipeline.addLast("client-outbound-stage-1", transmitter)
    }
  }

  private
  def setupBootstrap(): Unit = {
    ioMode match {
      case FabricNetIoMode.EPollIoMode =>
        _eventLoopGroup = new EpollEventLoopGroup()
        _transportClass = classOf[EpollSocketChannel]

      case FabricNetIoMode.NioIoMode =>
        _eventLoopGroup = new NioEventLoopGroup()
        _transportClass = classOf[NioSocketChannel]

      case FabricNetIoMode.KqIoMode =>
        _eventLoopGroup = new KQueueEventLoopGroup()
        _transportClass = classOf[KQueueSocketChannel]

      case _ =>
        throw VitalsException(s"$serviceName: client failed startup, invalid io mode $ioMode")
    }

    _bootstrap.group(_eventLoopGroup).channel(_transportClass)
    setNettyOptions(_bootstrap).handler(initializer)
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    synchronized {
      _stopping.set(false)
      _bootstrap = new Bootstrap
      setupBootstrap()
      log info startingMessage

      markRunning

      // schedule first connect
      scheduleConnect(10 millis)

      this
    }
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      _stopping.set(true)
      log info stoppingMessage
      try
        _clientChannel.close.syncUninterruptibly
      finally
        _eventLoopGroup.shutdownGracefully

      _eventLoopGroup = null
      _clientChannel = null
      _bootstrap = null

      markNotRunning

      this
    }
  }

  /**
   * do a connection operation here. We will have to do this at boot and each time
   * we 'lose' connection to the server (master)
   *
   * @return
   */
  private
  def connectToServer(): Unit = {
    lazy val tag = s"FabricNetClient.connectToServer(address=${_socketAddress})"
    if (isClosed) return

    val channelFuture = _bootstrap.connect(_socketAddress)

    channelFuture.addListener(new ChannelFutureListener {
      override def operationComplete(future: ChannelFuture): Unit = {
        if (isClosed) return

        if (!future.isSuccess) {
          val cause = future.cause
          log warn burstStdMsg(s"FAB_NET_CLIENT_FAILED_STARTUP to ${_socketAddress} cause=${cause.getLocalizedMessage} $tag", cause)
          // Stop the connection
          if (_connection.isRunning)
            _connection.stop
          future.channel().close()
          // go around again
          _bootstrap.connect(_socketAddress).addListener(this)
        } else {
          _clientChannel = future.channel()
          log info s"FAB_NET_CLIENT_COMPLETED_STARTUP to ${_socketAddress} $tag"
          future.channel().closeFuture().addListener(new ChannelFutureListener {
            override def operationComplete(future: ChannelFuture): Unit = {
              if (isClosed) return

              log warn s"FAB_NET_CLIENT_LOST_CONNECTION to ${_socketAddress} $tag"
              if (_connection.isRunning) _connection.stop
              scheduleConnect(1 second)
            }
          })
        }
      }
    })
  }

  private def isClosed: Boolean = _stopping.get || !isRunning

  private def scheduleConnect(wait: FiniteDuration): Unit = {
    _timer.schedule(new TimerTask() {
      def run(): Unit = {
        connectToServer()
      }
    }, wait.toMillis)
  }
}
