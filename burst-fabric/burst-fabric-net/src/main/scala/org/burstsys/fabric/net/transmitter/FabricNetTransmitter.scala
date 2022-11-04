/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.transmitter

import org.burstsys.fabric.container.model.FabricContainer
import org.burstsys.fabric.net.message.FabricNetMsg
import org.burstsys.fabric.net.{FabricNetLink, FabricNetReporter, debugFabNet}
import org.burstsys.tesla.thread
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.errors.{VitalsException, safely}
import io.netty.channel.{Channel, ChannelHandlerContext, ChannelOutboundHandlerAdapter, ChannelPromise}
import io.netty.util.concurrent.{GenericFutureListener, Future => NettyFuture}

import scala.concurrent.{Future, Promise}
import org.burstsys.vitals.logging._

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

  override
  def disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise): Unit = {
    super.disconnect(ctx, promise)
    log warn s"$this CHANNEL DISCONNECT"
  }

  /**
   * transmit a control plane message. These are immediately
   * flushed through the pipeline. The expensive part of this
   * network write is done on a channel thread asynchronously
   */
  def transmitControlMessage(msg: FabricNetMsg): Future[Unit] = {
    lazy val tag = s"FabricNetTransmitter.transmitControlMessage(${msg.getClass.getSimpleName} ${remoteAddress}:${remotePort}"
    val promise = Promise[Unit]()

    if (!channel.isOpen || !channel.isActive) {
      val msg = s"$tag cannot transmit channelOpen=${channel.isOpen} channelActive=${channel.isActive} "
      if (debugFabNet)
        log warn burstStdMsg(msg)
      promise.failure(VitalsException(msg).fillInStackTrace())
      return promise.future
    }

    // do the write/flush on the channel thread, because netty likes it that way
    channel.eventLoop.submit(new Runnable {
      override def run(): Unit = {
        try{
          val encodeStart = System.nanoTime
          val buffer = channel.alloc().buffer()
          msg.encode(buffer)
          val buffSize = buffer.capacity
          val encodeDuration = System.nanoTime - encodeStart
          val transmitEnqueued = System.nanoTime
          channel.writeAndFlush(buffer).addListener(new GenericFutureListener[NettyFuture[_ >: Void]] {
            override def operationComplete(future: NettyFuture[_ >: Void]): Unit = {
              val transmitDuration = System.nanoTime - transmitEnqueued
              log debug s"$tag encodeNanos=$encodeDuration transmitNanos=$transmitDuration"
              if (future.isSuccess) {
                FabricNetReporter.onMessageXmit(buffSize)
                promise.success(())
              } else {
                log warn burstStdMsg(s"$tag FAIL  ${future.cause}", future.cause)
                if (!promise.isCompleted)
                  promise.failure(future.cause())
              }
            }
          })
        } catch safely {
          case t:Throwable  =>
            log error burstStdMsg(s"XMIT_FAIL $t $tag", t)
            promise.failure(t)
        }
      }
    })
    promise.future
  }

  /**
   * transmit a data plane message. These are immediately
   * flushed through the pipeline. The expensive part of this
   * network write is done on a channel thread asynchronously
   *
   * @param msg
   */
  def transmitDataMessage(msg: FabricNetMsg): Future[Unit] = {
    lazy val tag = s"FabricNetTransmitter.transmitDataMessage(${msg.getClass.getName} ${remotePort}:${remotePort}"
    val promise = Promise[Unit]()

    if (!channel.isOpen || !channel.isActive) {
      val msg = s"$tag cannot transmit channelOpen=${channel.isOpen} channelActive=${channel.isActive}"
      log warn burstStdMsg(msg)
      promise.failure(VitalsException(msg).fillInStackTrace())
      return promise.future
    }

    // submit the write/flush on the channel thread, because netty likes it that way
    channel.eventLoop.submit(new Runnable {
      override def run(): Unit = {
        TeslaRequestCoupler { // TODO why is this necessary? can't we do the encode before the submit??
          try {
            val encodeStart = System.nanoTime
            val buffer = channel.alloc().buffer()
            msg.encode(buffer)
            val buffSize = buffer.capacity
            val encodeDuration = System.nanoTime - encodeStart

            val transmitEnqueued = System.nanoTime
            channel.writeAndFlush(buffer).addListener(new GenericFutureListener[NettyFuture[_ >: Void]] {
              override def operationComplete(future: NettyFuture[_ >: Void]): Unit = {
                val transmitDuration = System.nanoTime - transmitEnqueued
                log debug s"$tag encodeNanos=$encodeDuration transmitNanos=$transmitDuration"
                if (future.isSuccess) {
                  FabricNetReporter.onMessageXmit(buffSize)
                  promise.success(())
                } else {
                  log warn burstStdMsg(s"$tag FAIL  ${future.cause}", future.cause)
                  if (!promise.isCompleted)
                    promise.failure(future.cause())
                }
              }
            })
          } catch safely {
            case t:Throwable  =>
              log error burstStdMsg(s"XMIT_FAIL $t $tag", t)
              promise.failure(t)
          }
        }
      }
    })
    promise.future
  }

}
