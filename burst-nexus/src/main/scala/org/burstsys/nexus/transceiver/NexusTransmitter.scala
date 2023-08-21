/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.transceiver

import io.netty.channel.{Channel, ChannelHandlerContext, ChannelOutboundHandlerAdapter, ChannelPromise}
import io.netty.util.concurrent.{Future => NettyFuture}
import io.opentelemetry.context.Context
import org.burstsys.nexus.message.{NexusMsg, NexusStreamParcelMsg}
import org.burstsys.nexus.trek.NexusTransmitTrekMark
import org.burstsys.nexus.{NexusChannel, NexusReporter}
import org.burstsys.tesla
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.trek.context.injectContext

import scala.concurrent.{Future, Promise}

/**
 * outbound writes of control and data plane communication. Both the client and server share
 * this class (separate instances) but on the client side only the control plane is used.
 *
 * @param isServer        server or client
 * @param channel         the connected channel
 * @param maxQueuedWrites how many data batches in pipeline before flush
 */
final case
class NexusTransmitter(id: Int, isServer: Boolean, channel: Channel, maxQueuedWrites: Int = 5)
  extends ChannelOutboundHandlerAdapter with NexusChannel {

  override def toString: String = s"NexusTransmitter($link, ${if (isServer) "server" else "client"} id=$id, maxQueuedWrites=$maxQueuedWrites)"

  log debug s"NEXUS_TRANSMITTER_INIT $this"

  override def disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise): Unit = {
    super.disconnect(ctx, promise)
    log warn s"$this CHANNEL DISCONNECT"
  }

  /**
   * transmit a control plane message. These are immediately flushed through the pipeline
   *
   * @param msg the message to send
   */
  def transmitControlMessage(msg: NexusMsg): Future[Unit] = {
    val tag = s"NexusTransmitter.transmitControlMessage(${msg.getClass.getSimpleName} $remoteAddress:$remotePort"
    NexusTransmitTrekMark.begin() { stage =>
      stage.span.setAttribute(nexusMessageTypeKey, msg.messageType.code)
      try {
        if (!canSendMsg) {
          val ex = VitalsException(s"$tag channel not open or active").fillInStackTrace()
          NexusTransmitTrekMark.fail(stage, ex)
          return Promise.failed(ex).future
        }

        val promise = Promise[Unit]()
        val transmitStart = System.nanoTime
        // do the alloc/write/flush on the channel thread so the fabric thead is done right away
        channel.eventLoop.submit(Context.current().wrap(() => {
          val buffer = channel.alloc().buffer()
          injectContext(msg, buffer)
          msg.encode(buffer)
          val buffSize = buffer.capacity
          channel.writeAndFlush(buffer).addListener((future: NettyFuture[_ >: Void]) => {
            if (!future.isSuccess) {
              log warn burstStdMsg(s"$tag control message transmit failed  ${future.cause}", future.cause)
              promise.failure(future.cause())
              NexusTransmitTrekMark.fail(stage, future.cause())
            } else {
              NexusReporter.onTransmit(ns = System.nanoTime - transmitStart, bytes = buffSize)
              promise.success((): Unit)
              NexusTransmitTrekMark.end(stage)
            }
          })
        }))
        promise.future
      } catch safely {
        case t: Throwable =>
          NexusTransmitTrekMark.fail(stage, t)
          throw t
      }
    }
  }

  /**
   * transmit a data plane message. These are immediately flushed through the pipeline
   */
  def transmitDataMessage(message: NexusStreamParcelMsg): Future[Unit] = {
    val tag = s"NexusTransmitter.transmitDataMessage($remoteAddress:$remotePort"
    if (!canSendMsg) return Promise.failed(
      VitalsException(s"$tag channel not open or active").fillInStackTrace()
    ).future

    val promise = Promise[Unit]()
    val transmitStart = System.nanoTime
    channel.eventLoop.submit(Context.current.wrap(() => {
      val buffer = channel.alloc().buffer(message.parcel.currentUsedMemory)
      injectContext(message, buffer)
      message.encode(buffer)
      tesla.parcel.factory releaseParcel message.parcel
      val buffSize = buffer.capacity

      channel.writeAndFlush(buffer).addListener((future: NettyFuture[_ >: Void]) => {
        if (!future.isSuccess) {
          log warn burstStdMsg(s"$tag data message transmit failed  ${future.cause}", future.cause)
          promise.failure(future.cause())
        } else {
          NexusReporter.onTransmit(ns = System.nanoTime - transmitStart, bytes = buffSize)
          promise.success(())
        }
      })
    }))
    promise.future
  }

  private def canSendMsg: Boolean = channel.isOpen && channel.isActive
}
