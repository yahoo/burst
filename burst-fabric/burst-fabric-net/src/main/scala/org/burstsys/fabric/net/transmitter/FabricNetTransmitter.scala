/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.transmitter

import io.netty.channel.{Channel, ChannelHandlerContext, ChannelOutboundHandlerAdapter, ChannelPromise}
import io.opentelemetry.context.Context
import org.burstsys.fabric.container.FabricContainer
import org.burstsys.fabric.net.FabricNetLink
import org.burstsys.fabric.net.message.FabricNetMsg
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging._

import java.net.SocketAddress
import scala.concurrent.{Future, Promise}

/**
 * outbound writes of control and data plane communication. Both the client and server share
 * this class (separate instances) but on the client side only the control plane is used.
 *
 * @param isServer    server or client
 * @param channel     the connected channel
 * @param maxInFlight how many data batches in pipeline before flush
 */
final case
class FabricNetTransmitter(container: FabricContainer, isServer: Boolean, channel: Channel, maxInFlight: Int = 5)
  extends ChannelOutboundHandlerAdapter with FabricNetLink {

  override def toString: String = s"FabricNetTransmitter($link, ${if (isServer) "server" else "client"} containerId=${container.containerId})"

  override def bind(ctx: ChannelHandlerContext, localAddress: SocketAddress, promise: ChannelPromise): Unit = {
    super.bind(ctx, localAddress, promise)
    log debug burstStdMsg(s"CHANNEL_BIND $this")
  }

  override def connect(ctx: ChannelHandlerContext, remoteAddress: SocketAddress, localAddress: SocketAddress, promise: ChannelPromise): Unit = {
    super.connect(ctx, remoteAddress, localAddress, promise)
    log debug burstStdMsg(s"CHANNEL_CONNECT $this")
  }

  override def disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise): Unit = {
    super.disconnect(ctx, promise)
    log debug burstStdMsg(s"CHANNEL_DISCONNECT $this")
  }

  override def close(ctx: ChannelHandlerContext, promise: ChannelPromise): Unit = {
    super.close(ctx, promise)
    log debug burstStdMsg(s"CHANNEL_CLOSE $this")
  }

  override def deregister(ctx: ChannelHandlerContext, promise: ChannelPromise): Unit = {
    super.deregister(ctx, promise)
    log debug burstStdMsg(s"CHANNEL_DEREGISTER $this")
  }

  /**
   * transmit a control plane message. These are immediately
   * flushed through the pipeline. The expensive part of this
   * network write is done on a channel thread asynchronously
   */
  def transmitControlMessage(msg: FabricNetMsg): Future[Unit] = {
    val tag = s"FabricNetTransmitter.transmitControlMessage(${msg.getClass.getSimpleName} $remoteAddress:$remotePort)"

    if (!channel.isOpen || !channel.isActive) {
      val msg = s"$tag cannot transmit channelOpen=${channel.isOpen} channelActive=${channel.isActive} "
      log trace burstStdMsg(msg)
      return Future.failed(VitalsException(msg).fillInStackTrace())
    }

    val nettySend = new NettyMessageSendRunnable(channel, msg, tag)
    channel.eventLoop.submit(Context.current().wrap(nettySend))
    nettySend.completion
  }

  /**
   * transmit a data plane message. These are immediately
   * flushed through the pipeline. The expensive part of this
   * network write is done on a channel thread asynchronously
   */
  def transmitDataMessage(msg: FabricNetMsg): Future[Unit] = {
    val tag = s"FabricNetTransmitter.transmitDataMessage(${msg.getClass.getName} $remotePort:$remotePort"

    if (!channel.isOpen || !channel.isActive) {
      val msg = s"$tag cannot transmit channelOpen=${channel.isOpen} channelActive=${channel.isActive}"
      log warn burstStdMsg(msg)
      return Future.failed(VitalsException(msg).fillInStackTrace())
    }

    val nettySend = new NettyMessageSendRunnable(channel, msg, tag)
    channel.eventLoop.submit(Context.current().wrap(nettySend))
    nettySend.completion
  }

}
