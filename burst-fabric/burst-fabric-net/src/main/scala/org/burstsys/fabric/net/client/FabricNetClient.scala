/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollSocketChannel}
import io.netty.channel.kqueue.{KQueueEventLoopGroup, KQueueSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import org.burstsys.fabric.container.FabricWorkerService
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.net.FabricNetIoMode.FabricNetIoMode
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message.assess.FabricNetAssessReqMsg
import org.burstsys.fabric.net.message.{AccessParameters, FabricNetInboundFrameDecoder, FabricNetOutboundFrameEncoder}
import org.burstsys.fabric.net.transceiver.{FabricNetReceiver, FabricNetTransmitter}
import org.burstsys.fabric.net.{FabricNetIoMode, FabricNetLink, FabricNetworkConfig, message}
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.healthcheck.VitalsHealthMonitoredService
import org.burstsys.vitals.logging._

import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.{Timer, TimerTask}
import scala.annotation.unused
import scala.concurrent.duration.Duration._
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 * a fabric network client (worker side)
 */
trait FabricNetClient extends FabricWorkerService with FabricNetLink with FabricNetClientListener {

  def talksTo(listeners: FabricNetClientListener*): this.type

  def connection: FabricNetClientConnection

}

object FabricNetClient {

  def apply(container: FabricWorkerContainer[_], netConfig: FabricNetworkConfig): FabricNetClient =
    FabricNetClientContext(container, netConfig)

}

private[client] final case
class FabricNetClientContext(container: FabricWorkerContainer[_], netConfig: FabricNetworkConfig) extends FabricNetClient
  with FabricNetClientNetty with VitalsHealthMonitoredService {

  override def toString: String = serviceName

  override def serviceName: String = s"fabric-net-client(containerId=${container.containerIdGetOrThrow}, ${netConfig.netSupervisorUrl})"

  val modality: VitalsServiceModality = VitalsPojo

  ////////////////////////////////////////////////////////////////////////////////////
  // Config
  ////////////////////////////////////////////////////////////////////////////////////

  def ioMode: FabricNetIoMode = FabricNetIoMode.NioIoMode

  def timeout: Duration = Inf

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _clientChannel: Channel = _

  private[this]
  var _transportClass: Class[_ <: Channel] = _

  private[this]
  var _socketAddress: SocketAddress = netConfig.netSupervisorSocketAddress

  private[this]
  var _eventLoopGroup: EventLoopGroup = _

  private[this]
  var _connection: FabricNetClientConnection = _

  private[this]
  val _listenerSet = ConcurrentHashMap.newKeySet[FabricNetClientListener]

  private[this]
  val _stopping: AtomicBoolean = new AtomicBoolean(false)

  private[this]
  var _bootstrap: Bootstrap = _

  private[this]
  val _timer: Timer = new Timer()

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override def channel: Channel = _clientChannel

  override
  def talksTo(listeners: FabricNetClientListener*): this.type = {
    _listenerSet.addAll(listeners.asJava)
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
      )
      container.health.registerComponent(_connection)
      connection.talksTo(FabricNetClientContext.this)
      connection.talksTo(_listenerSet.asScala.toSeq: _*)
      connection.start

      // inbound goes in forward pipeline order
      clientPipeline.addLast("client-inbound-stage-1", FabricNetInboundFrameDecoder())
      clientPipeline.addLast("client-inbound-stage-2", receiver)

      // outbound goes in reverse pipeline order
      clientPipeline.addLast("client-outbound-stage-2", FabricNetOutboundFrameEncoder())
      clientPipeline.addLast("client-outbound-stage-1", transmitter)
    }
  }

  private def setupBootstrap(): Unit = {
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

  override def start: this.type = {
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

  override def stop: this.type = {
    synchronized {
      ensureRunning
      _stopping.set(true)
      log info stoppingMessage
      _connection.stopIfNotAlreadyStopped
      try {
        _clientChannel.close.syncUninterruptibly
      } finally _eventLoopGroup.shutdownGracefully

      _eventLoopGroup = null
      _clientChannel = null
      _bootstrap = null

      markNotRunning

      this
    }
  }

  /**
   * do a connection operation here. We will have to do this at boot and each time
   * we lose connection to the server (supervisor)
   */
  private def connectToServer(): Unit = {
    if (isClosed) {
      return
    }
    _socketAddress = netConfig.netSupervisorSocketAddress
    _bootstrap.connect(_socketAddress).addListener(connectionHandler)
  }

  /**
   * A channel listener that ensures that we get our initial connection to
   * @param future
   */
  private def connectionHandler(future: ChannelFuture): Unit = {
    lazy val tag = s"FabricNetClient.connectionHandler(address=${_socketAddress})"
    if (isClosed) {
      return
    }
    if (future.isSuccess) {
      _clientChannel = future.channel()
      log info burstStdMsg(s"FAB_NET_CLIENT_CONNECTED to ${_socketAddress} $tag")
      future.channel().closeFuture().addListener(reconnectionHandler)

    } else {
      val cause = future.cause
      log warn burstStdMsg(s"FAB_NET_CLIENT_CONNECTION_FAILED to ${_socketAddress} cause=${cause.getLocalizedMessage} $tag", cause)
      // Stop the connection
      if (_connection.isRunning) {
        _connection.stop
      }
      future.channel().close()
      // go around again
      scheduleConnect(5 second)
    }
  }

  /**
   * A channel listener that attempts to reconnect to the supervisor when the connection is closed
   */
  private def reconnectionHandler(@unused future: ChannelFuture): Unit = {
    lazy val tag = s"FabricNetClient.reconnectionHandler(address=${_socketAddress})"
    if (isClosed) {
      return
    }
    log warn burstStdMsg(s"FAB_NET_CLIENT_LOST_CONNECTION to ${_socketAddress} $tag")
    if (_connection.isRunning) {
      _connection.stop
    }
    scheduleConnect(1 second)
  }

  private def isClosed: Boolean = _stopping.get || !isRunning

  private def scheduleConnect(wait: FiniteDuration): Unit = {
    _timer.schedule(new TimerTask() {
      def run(): Unit = {
        connectToServer()
      }
    }, wait.toMillis)
  }

  override def onNetMessage(connection: FabricNetClientConnection, messageId: message.FabricNetMsgType, buffer: Array[Byte]): Unit = {
    container.onNetMessage(connection, messageId, buffer)
  }

  override def onDisconnect(connection: FabricNetClientConnection): Unit = {
    log trace burstStdMsg(s"disconnect ${_socketAddress}")
    container.onDisconnect(connection)
  }

  override
  def onNetClientAssessReqMsg(connection: FabricNetClientConnection, msg: FabricNetAssessReqMsg): Unit = {
    container.onNetClientAssessReqMsg(connection, msg)
  }

  override def connection: FabricNetClientConnection = _connection

  override def prepareAccessParameters(parameters: AccessParameters): AccessParameters = {
    container.prepareAccessParameters(parameters)
  }
}
