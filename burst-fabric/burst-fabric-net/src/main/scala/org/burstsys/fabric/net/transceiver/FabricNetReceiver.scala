/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.transceiver

import io.netty.buffer.ByteBuf
import io.netty.channel.{Channel, ChannelHandlerContext, SimpleChannelInboundHandler}
import org.burstsys.fabric.container.FabricContainer
import org.burstsys.fabric.net.message._
import org.burstsys.fabric.net.{FabricNetConnection, FabricNetLink, FabricNetReporter}
import org.burstsys.fabric.trek.FabricNetReceive
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.trek.context.extractContext

import scala.annotation.unused

/**
 * receive inbound messages in the form of netty [[ByteBuf]], identity and convert
 * to [[org.burstsys.fabric.net.message.FabricNetMsg]] instances and then dispatch to either a server or client listener.
 * Server and client share this received even though each will only get a subset of all
 * possible messages.
 */
final case
class FabricNetReceiver(container: FabricContainer, isServer: Boolean, channel: Channel)
  extends SimpleChannelInboundHandler[ByteBuf] with FabricNetLink {

  private var connection: Option[FabricNetConnection] = None

  override def toString: String = s"FabricNetReceiver(containerId=${container.containerId.getOrElse("Not Set")}, $link)"

  def connectedTo(connection: FabricNetConnection): this.type = {
    this.connection = Option(connection)
    this
  }

  override def channelRegistered(ctx: ChannelHandlerContext): Unit = {
    log debug burstStdMsg(s"CHANNEL_REGISTERED $this")
    super.channelRegistered(ctx)
  }

  override def channelUnregistered(ctx: ChannelHandlerContext): Unit = {
    log debug burstStdMsg(s"CHANNEL_UNREGISTERED $this")
    super.channelUnregistered(ctx)
  }

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    log info burstStdMsg(s"CHANNEL_ACTIVE $this")
    super.channelActive(ctx)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    super.channelInactive(ctx)
    log info burstStdMsg(s"CHANNEL_INACTIVE $this ")
    connection.foreach(_.onDisconnect())
  }

  override def channelRead0(ctx: ChannelHandlerContext, buffer: ByteBuf): Unit = {
    // gather basics
    @unused val _ = buffer.readInt() // first field is the message length
    val scp = extractContext(this, buffer)
    try {
      val stage = FabricNetReceive.beginSync()
      try {
        val messageTypeKey = buffer.readInt()
        stage.span.setAttribute(fabricMessageTypeKey, messageTypeKey)
        FabricNetReporter.onMessageRecv(buffer.capacity)
        val bytes: Array[Byte] = {
          val oldPosition = buffer.nioBuffer().position()
          val array = new Array[Byte](buffer.nioBuffer().remaining)
          buffer.nioBuffer().get(array, 0, array.length)
          buffer.nioBuffer().position(oldPosition)
          array
        }

        TeslaRequestFuture {
          val msgType = FabricNetMsgType(messageTypeKey)
          connection match {
            case None =>
              val message = burstStdMsg(s"FAB_NET_NO_CONNECTION $this $msgType")
              FabricNetReceive.fail(stage, VitalsException(message))
              log warn message
            case Some(connection) =>
              FabricNetReceive.end(stage)
              connection.onMessage(msgType, bytes)
          }
        }
      } catch safely {
        case t: Throwable =>
          FabricNetReceive.fail(stage, t)
          throw t
      } finally stage.closeScope()
    } finally scp.close()
  }
}
