/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.receiver

import org.burstsys.nexus.client.NexusClientReporter
import org.burstsys.nexus.message._
import org.burstsys.nexus.server.NexusServerReporter
import org.burstsys.nexus.transmitter.NexusTransmitter
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors._
import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import org.burstsys.vitals.logging._

import scala.annotation.unused

/**
 * receive inbound messages in the form of netty [[ByteBuf]], identity and convert
 * to [[NexusMsg]] instances and then dispatch to either a server or client listener.
 * Server and client share this received even though each will only get a subset of all
 * possible messages.
 */
final case
class NexusReceiver(
                     id: Int,
                     isServer: Boolean,
                     transmitter: NexusTransmitter,
                     clientListener: NexusClientMsgListener = null,
                     serverListener: NexusServerMsgListener = null,
                     disconnectCallback: () => Unit = null
                   ) extends SimpleChannelInboundHandler[ByteBuf] {

  val debug = false

  override def toString: String = s"NexusReceiver(${if (isServer) "server" else "client"} id=$id ${transmitter.link})"

  override def channelRegistered(ctx: ChannelHandlerContext): Unit = {
    super.channelRegistered(ctx)
    if (isServer)
      NexusServerReporter.onServerConnectionStart()
    else
      NexusClientReporter.onClientConnectionStart()
  }

  override def channelUnregistered(ctx: ChannelHandlerContext): Unit = {
    super.channelUnregistered(ctx)
    if (isServer)
      NexusServerReporter.onServerConnectionStop()
    else
      NexusClientReporter.onClientConnectionStop()
  }

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    super.channelActive(ctx)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    super.channelInactive(ctx)
    log debug burstStdMsg(s"NEXUS_CHANNEL_INACTIVE $this")
    if (disconnectCallback != null) {
      disconnectCallback()
    }
  }

  override def channelRead0(ctx: ChannelHandlerContext, buffer: ByteBuf): Unit = {
    // gather basics
    @unused val messageLength = buffer.readInt()
    val messageTypeKey = buffer.readInt()

    TeslaRequestCoupler {
      // pass thread control from netty pool to tesla pool for parts pool access
      NexusMsgType(messageTypeKey) match {
        case NexusStreamInitiatedMsgType =>
          dispatchMessage(NexusStreamInitiatedMsg(buffer), clientListener.onStreamInitiatedMsg)

        case NexusStreamParcelMsgType =>
          dispatchMessage(NexusStreamParcelMsg(buffer), clientListener.onStreamParcelMsg)

        case NexusStreamCompleteMsgType =>
          dispatchMessage(NexusStreamCompleteMsg(buffer), clientListener.onStreamCompleteMsg)

        case NexusStreamHeartbeatMsgType =>
          dispatchMessage(NexusStreamHeartbeatMsg(buffer), clientListener.onStreamHeartbeatMsg)

        case NexusStreamInitiateMsgType =>
          dispatchMessage(NexusStreamInitiateMsg(buffer), serverListener.onStreamInitiateMsg)

        case NexusStreamAbortMsgType =>
          dispatchMessage(NexusStreamAbortMsg(buffer), serverListener.onStreamAbortMsg)

        case mt =>
          log warn burstStdMsg(s"Unknown message type: $mt")
          ???
      }
    }
  }

  private def dispatchMessage[T <: NexusMsg](msg: T, cb: T => Unit): Unit = {
    if (debug)
      log info s"$this ${msg.getClass.getSimpleName}"
    val oldName = Thread.currentThread().getName
    Thread.currentThread().setName("nexus-msg-recv")
    try {
      cb(msg)
    } catch safely {
      case t: Throwable =>
        log error burstLocMsg(s"Failed to dispatch: $msg", t)
    } finally Thread.currentThread().setName(oldName)
  }

}
