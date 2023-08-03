/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.receiver

import io.netty.buffer.ByteBuf
import io.netty.channel.{Channel, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.context.{Context, Scope}
import org.burstsys.fabric.container.FabricContainer
import org.burstsys.fabric.net.message._
import org.burstsys.fabric.net.{FabricNetConnection, FabricNetLink, FabricNetReporter}
import org.burstsys.tesla.thread.request.{TeslaRequestCoupler, TeslaRequestFuture}
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
      val messageTypeKey = buffer.readInt()
      FabricNetReporter.onMessageRecv(buffer.capacity)

      TeslaRequestCoupler {
        val messageId = FabricNetMsgType(messageTypeKey)
        connection match {
          case None =>
            log warn burstStdMsg(s"FAB_NET_NO_CONNECTION $this $messageId")
          case Some(connection) =>
            val bytes: Array[Byte] = {
              val oldPosition = buffer.nioBuffer().position()
              val array = new Array[Byte](buffer.nioBuffer().remaining)
              buffer.nioBuffer().get(array, 0, array.length)
              buffer.nioBuffer().position(oldPosition)
              array
            }
            TeslaRequestFuture {
              try {
                connection.onMessage(messageId, bytes)
              } catch safely {
                case t: Throwable =>
                  log error(burstStdMsg(s"FAB_NET_RECEIVER_DISPATCH_FAIL $this", t), t)
              }
            }
        }
      }
    } finally {
      scp.close()
    }
  }
}
