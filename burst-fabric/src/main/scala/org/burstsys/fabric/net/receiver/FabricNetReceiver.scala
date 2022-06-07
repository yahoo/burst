/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.receiver

import org.burstsys.fabric.container.model.FabricContainer
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.message._
import org.burstsys.fabric.net.message.assess.{FabricNetAssessReqMsg, FabricNetAssessRespMsg, FabricNetTetherMsg}
import org.burstsys.fabric.net.message.cache._
import org.burstsys.fabric.net.message.scatter.FabricNetProgressMsg
import org.burstsys.fabric.net.message.wave.{FabricNetParticleReqMsg, FabricNetParticleRespMsg}
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.net.{FabricNetLink, FabricNetReporter}
import org.burstsys.tesla.thread.request.{TeslaRequestCoupler, TeslaRequestFuture}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import io.netty.buffer.ByteBuf
import io.netty.channel.{Channel, ChannelHandlerContext, SimpleChannelInboundHandler}

/**
 * receive inbound messages in the form of netty [[ByteBuf]], identity and convert
 * to [[org.burstsys.fabric.net.message.FabricNetMsg]] instances and then dispatch to either a server or client listener.
 * Server and client share this received even though each will only get a subset of all
 * possible messages.
 */
final case
class FabricNetReceiver(container: FabricContainer, isServer: Boolean, channel: Channel)
  extends SimpleChannelInboundHandler[ByteBuf] with FabricNetLink {

  override def toString: String = s"FabricNetReceiver(containerId=${container.containerIdGetOrThrow}, $link)"

  private[this]
  var serverConnection: Option[FabricNetServerConnection] = None

  def connectedTo(connection: FabricNetServerConnection): this.type = {
    serverConnection = Option(connection)
    this
  }

  private[this]
  var clientConnection: Option[FabricNetClientConnection] = None

  def connectedTo(connection: FabricNetClientConnection): this.type = {
    clientConnection = Option(connection)
    this
  }

  override
  def channelRegistered(ctx: ChannelHandlerContext): Unit = {
    log trace s"$this CHANNEL REGISTERED"
    super.channelRegistered(ctx)
  }

  override
  def channelUnregistered(ctx: ChannelHandlerContext): Unit = {
    log trace s"$this CHANNEL UNREGISTERED"
    super.channelUnregistered(ctx)
  }

  override
  def channelActive(ctx: ChannelHandlerContext): Unit = {
    log debug s"$this CHANNEL ACTIVE"
    super.channelActive(ctx)
  }

  override
  def channelInactive(ctx: ChannelHandlerContext): Unit = {
    super.channelInactive(ctx)
    log warn s"FAB_NET_CHANNEL_INACTIVE $this "
    clientConnection.foreach(c => c.onNetClientServerDisconnect(c))
    serverConnection.foreach(s => s.onNetServerDisconnect(s))
  }

  private val noServerTag = "FAB_NET_NO_SERVER_CONNECTION"
  private val noClientTag = "FAB_NET_NO_CLIENT_CONNECTION"

  override
  def channelRead0(ctx: ChannelHandlerContext, buffer: ByteBuf): Unit = {
    // gather basics
    buffer.readInt() // first field is the message length
    val messageTypeKey = buffer.readInt()
    FabricNetReporter.onMessageRecv(buffer.capacity)

    TeslaRequestCoupler {
      FabricNetMsgType(messageTypeKey) match {

        /////////////////// TETHERING /////////////////
        case FabricNetTetherMsgType =>
          serverConnection match {
            case None => log warn s"$noServerTag FabricNetTetherMsgType"
            case Some(c) =>
              val msg = FabricNetTetherMsg(buffer)
              dispatch(c.onNetServerTetherMsg(c, msg))
          }

        /////////////////// PARTICLES /////////////////
        case FabricNetParticleReqMsgType =>
          clientConnection match {
            case None => log warn s"$noClientTag FabricNetParticleReqMsgType"
            case Some(c) =>
              val msg = FabricNetParticleReqMsg(buffer)
              dispatch(c.onNetClientParticleReqMsg(c, msg))
          }

        case FabricNetProgressMsgType =>
          serverConnection match {
            case None => log warn s"$noServerTag FabricNetProgressMsgType"
            case Some(c) =>
              val msg = FabricNetProgressMsg(buffer)
              dispatch(c.onNetServerParticleProgressMsg(c, msg))
          }

        case FabricNetParticleRespMsgType =>
          serverConnection match {
            case None => log warn s"$noServerTag FabricNetParticleRespMsgType"
            case Some(c) =>
              val msg = FabricNetParticleRespMsg(buffer)
              dispatch(c.onNetServerParticleRespMsg(c, msg))
          }

        /////////////////// ASSESSMENT /////////////////
        case FabricNetAssessReqMsgType =>
          clientConnection match {
            case None => log warn s"$noClientTag FabricNetAssessReqMsgType"
            case Some(c) =>
              val msg = FabricNetAssessReqMsg(buffer)
              dispatch(c.onNetClientAssessReqMsg(c, msg))
          }

        case FabricNetAssessRespMsgType =>
          serverConnection match {
            case None => log warn s"$noServerTag FabricNetAssessRespMsgType"
            case Some(c) =>
              val msg = FabricNetAssessRespMsg(buffer)
              dispatch(c.onNetServerAssessRespMsg(c, msg))
          }

        /////////////////// CACHE OPERATIONS /////////////////
        case FabricNetCacheOperationReqMsgType =>
          clientConnection match {
            case None => log warn s"$noClientTag FabricNetCacheOperationReqMsgType"
            case Some(c) =>
              val msg = FabricNetCacheOperationReqMsg(buffer)
              dispatch(c.onNetClientCacheOperationReqMsg(c, msg))
          }

        case FabricNetCacheOperationRespMsgType =>
          serverConnection match {
            case None => log warn s"$noServerTag FabricNetCacheOperationRespMsgType"
            case Some(c) =>
              val msg = FabricNetCacheOperationRespMsg(buffer)
              dispatch(c.onNetServerCacheOperationRespMsg(c, msg))
          }

        /////////////////// CACHE SLICE FETCH /////////////////
        case FabricNetSliceFetchReqMsgType =>
          clientConnection match {
            case None => log warn s"$noClientTag FabricNetSliceFetchReqMsgType"
            case Some(c) =>
              val msg = FabricNetSliceFetchReqMsg(buffer)
              dispatch(c.onNetClientSliceFetchReqMsg(c, msg))
          }

        case FabricNetSliceFetchRespMsgType =>
          serverConnection match {
            case None => log warn s"$noServerTag FabricNetSliceFetchRespMsgType"
            case Some(c) =>
              val msg = FabricNetSliceFetchRespMsg(buffer)
              dispatch(c.onNetServerSliceFetchRespMsg(c, msg))
          }

        case mt =>
          log warn burstStdMsg(s"Unknown message type $mt")
          ???
      }
    }
  }

  object dispatch {
    final def apply(body: => Unit): Unit = {
      // fab net is not streaming i.e. no dispatch ordering is necessary
      TeslaRequestFuture {
        try {
          body
        } catch safely {
          case t: Throwable =>
            log error burstStdMsg(s"FAB_NET_RECEIVER_DISPATCH_FAIL $t", t)
        }
      }
    }
  }

}
