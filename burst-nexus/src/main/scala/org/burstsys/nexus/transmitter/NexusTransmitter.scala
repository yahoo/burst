/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.transmitter

import org.burstsys.nexus.message.{NexusMsg, NexusStreamCompleteMsg, NexusStreamHeartbeatMsg, NexusStreamParcelMsg, maxFrameLength}
import org.burstsys.nexus.{NexusChannel, NexusReporter}
import org.burstsys.vitals.errors.{VitalsException, safely}
import io.netty.buffer.ByteBuf
import io.netty.channel.{Channel, ChannelHandlerContext, ChannelOutboundHandlerAdapter, ChannelPromise}
import io.netty.util.concurrent.{GenericFutureListener, Future => NettyFuture}
import org.burstsys.nexus.server.NexusServerReporter
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.trek.{NexusServerCompleteSendTrekMark, NexusServerParcelSendTrekMark, NexusServerStreamTrekMark}
import org.burstsys.tesla
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}

import scala.concurrent.{Await, Future, Promise}
import org.burstsys.vitals.logging._

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

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

  def transmitDataStream(stream: NexusStream): Unit = {
    lazy val tag = s"NexusParcelTransmitter.transmitDataStream(id=$id, $link, stream=$stream  ${remoteAddress}:${remotePort})"
    val streamSpan = NexusServerStreamTrekMark.begin(stream.guid)
    TeslaRequestFuture {
      try {
        var lastMessageTransmit: Future[Unit] = Promise.successful((): Unit).future

        var moreToGo = true
        while (moreToGo) {
          val parcel = stream.take
          if (parcel.status.isHeartbeat) {
            NexusServerReporter.onServerHeartbeat()
            transmitControlMessage(NexusStreamHeartbeatMsg(stream))

          } else if (parcel.status.isMarker) {
            NexusServerReporter.onServerStreamSucceed()
            val sendStreamCompleteSpan = NexusServerCompleteSendTrekMark.begin(stream.guid)

            Await.ready(lastMessageTransmit, Duration.Inf)
            transmitControlMessage(NexusStreamCompleteMsg(stream, parcel.status)) onComplete {
              case Failure(t) =>
                log error burstStdMsg(s"$tag:$t", t)
                NexusServerCompleteSendTrekMark.fail(sendStreamCompleteSpan)
                NexusServerStreamTrekMark.fail(streamSpan)
                NexusServerReporter.onServerStreamFail()
              case Success(r) =>
                NexusServerCompleteSendTrekMark.end(sendStreamCompleteSpan)
                NexusServerStreamTrekMark.end(streamSpan)
            }
            moreToGo = false

          } else if (parcel.currentUsedMemory > maxFrameLength) {
            log error s"$tag parcel=$parcel size=${parcel.currentUsedMemory} exceeds maxFrameLength=$maxFrameLength, discarding parcel"
            NexusServerReporter.onServerDrop()
            tesla.parcel.factory releaseParcel parcel

          } else {
            Await.ready(lastMessageTransmit, Duration.Inf)

            log info s"NexusStreamParcelMsg guid=${stream.guid} suid=${stream.suid} count=${parcel.bufferCount} action=transmit"
            val writeStart = System.nanoTime
            val parcelSize = parcel.currentUsedMemory
            val update = NexusStreamParcelMsg(stream, parcel)
            val buffer = channel.alloc().buffer(parcel.currentUsedMemory)
            update.encode(buffer)
            tesla.parcel.factory releaseParcel parcel

            val sendParcelSpan = NexusServerParcelSendTrekMark.begin(stream.guid)
            lastMessageTransmit = transmitDataMessage(buffer)
            lastMessageTransmit onComplete {
              case Failure(t) =>
                NexusServerParcelSendTrekMark.fail(sendParcelSpan)
                log error burstStdMsg(s"$tag could not transmit parcel $t", t)
              case Success(_) =>
                NexusServerParcelSendTrekMark.end(sendParcelSpan)
            }
            // this measurement is going to take almost no time since the write is async
            NexusServerReporter.onServerWrite(parcelSize, System.nanoTime() - writeStart)
          }
        }
      } catch safely {
        case t: Throwable =>
          NexusServerStreamTrekMark.fail(streamSpan)
          NexusServerReporter.onServerStreamFail()
          log error burstStdMsg(s"$tag:$t", t)
      }
    }
  }

  /**
   * transmit a control plane message. These are immediately flushed through the pipeline
   *
   * @param msg the message to send
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
        channel.writeAndFlush(buffer).addListener((future: NettyFuture[_ >: Void]) => {
          if (!future.isSuccess) {
            log warn burstStdMsg(s"$tag control message transmit failed  ${future.cause}", future.cause)
            promise.failure(future.cause())
          } else {
            NexusReporter.onTransmit(ns = System.nanoTime - transmitStart, bytes = buffSize)
            promise.success((): Unit)
          }
        })
      }
    })
    promise.future
  }

  /**
   * transmit a data plane message. These are immediately flushed through the pipeline
   */
  def transmitDataMessage(buffer: ByteBuf): Future[Unit] = {
    val tag = s"NexusTransmitter.transmitDataMessage(${remoteAddress}:${remotePort}"
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
        channel.writeAndFlush(buffer).addListener((future: NettyFuture[_ >: Void]) => {
          if (!future.isSuccess) {
            log warn burstStdMsg(s"$tag data message transmit failed  ${future.cause}", future.cause)
            promise.failure(future.cause())
          } else {
            NexusReporter.onTransmit(ns = System.nanoTime - transmitStart, bytes = buffSize)
            promise.success(())
          }
        })
      }
    })
    promise.future
  }
}
