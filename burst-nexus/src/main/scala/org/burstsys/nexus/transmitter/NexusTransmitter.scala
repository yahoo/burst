/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.transmitter

import org.burstsys.nexus.message.NexusMsg
import org.burstsys.nexus.{NexusChannel, NexusReporter}
import org.burstsys.vitals.errors.VitalsException
import io.netty.buffer.ByteBuf
import io.netty.channel.{Channel, ChannelHandlerContext, ChannelOutboundHandlerAdapter, ChannelPromise}
import io.netty.util.concurrent.{GenericFutureListener, Future => NettyFuture}

import scala.concurrent.{Future, Promise}
import org.burstsys.vitals.logging._

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
  extends ChannelOutboundHandlerAdapter with NexusParcelTransmitter with NexusChannel {

  override def toString: String = s"NexusTransmitter($link, ${if (isServer) "server" else "client"} id=$id, maxQueuedWrites=$maxQueuedWrites)"

  log info s"NEXUS_TRANSMITTER_INIT $this"

  override
  def disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise): Unit = {
    super.disconnect(ctx, promise)
    log warn s"$this CHANNEL DISCONNECT"
  }

  /**
   * transmit a control plane message. These are immediately
   * flushed through the pipeline
   *
   * @param msg
   */
  def transmitControlMessage(msg: NexusMsg): Future[Unit] = {
    val tag = s"NexusTransmitter.transmitControlMessage(${msg.getClass.getSimpleName} ${remoteAddress}:${remotePort}"
    val promise = Promise[Unit]()
    if (!channel.isOpen || !channel.isActive) return {
      promise.failure(VitalsException(s"$tag channel not open or active").fillInStackTrace())
      promise.future
    }
    val transmitStart = System.nanoTime
    // do the alloc/write/flush on the channel thread so its done right away
    channel.eventLoop.submit(new Runnable {
      override def run(): Unit = {
        val buffer = channel.alloc().buffer()
        msg.encode(buffer)
        val buffSize = buffer.capacity
        channel.writeAndFlush(buffer).addListener(new GenericFutureListener[NettyFuture[_ >: Void]] {
          override def operationComplete(future: NettyFuture[_ >: Void]): Unit = {
            if (!future.isSuccess) {
              log warn burstStdMsg(s"$tag control message transmit failed  ${future.cause}", future.cause)
              promise.failure(future.cause())
            } else {
              NexusReporter.onTransmit(ns = System.nanoTime - transmitStart, bytes = buffSize)
              promise.success(())
            }
          }
        })
      }
    })
    promise.future
  }

  /**
   * transmit a data plane message. These are optionally immediately
   * flushed through the pipeline
   *
   * @param flush
   */
  def transmitDataMessage(buffer: ByteBuf, flush: Boolean): Future[Unit] = {
    val tag = s"NexusTransmitter.transmitDataMessage(flush=$flush ${remoteAddress}:${remotePort}"
    val promise = Promise[Unit]()
    if (!channel.isOpen || !channel.isActive) return {
      promise.failure(VitalsException(s"$tag channel not open or active").fillInStackTrace())
      promise.future
    }
    val transmitStart = System.nanoTime
    val buffSize = buffer.capacity
    // do the alloc/write/flush on the channel thread so its done right away
    channel.eventLoop.submit(new Runnable {
      override def run(): Unit = {
        channel.writeAndFlush(buffer).addListener(new GenericFutureListener[NettyFuture[_ >: Void]] {
          override def operationComplete(future: NettyFuture[_ >: Void]): Unit = {
            if (!future.isSuccess) {
              log warn burstStdMsg(s"$tag data message transmit failed  ${future.cause}", future.cause)
              promise.failure(future.cause())
            } else {
              NexusReporter.onTransmit(ns = System.nanoTime - transmitStart, bytes = buffSize)
              promise.success(())
            }
          }
        })
      }
    })
    promise.future
  }
}
