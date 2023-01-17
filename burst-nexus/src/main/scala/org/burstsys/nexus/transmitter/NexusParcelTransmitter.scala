/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.transmitter

import org.burstsys.nexus.message.{NexusStreamCompleteMsg, NexusStreamHeartbeatMsg, NexusStreamParcelMsg, maxFrameLength}
import org.burstsys.nexus.server.NexusServerReporter
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.trek.{NexusServerCompleteSendTrekMark, NexusServerParcelSendTrekMark, NexusServerStreamTrekMark}
import org.burstsys.tesla
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * data parcel stream transmit functionality
 */
trait NexusParcelTransmitter {

  self: NexusTransmitter =>

  final val slowSync = 1 seconds

  final
  def transmitDataStream(stream: NexusStream): Unit = {
    lazy val tag = s"NexusParcelTransmitter.transmitDataStream(id=$id, $link, stream=$stream  ${remoteAddress}:${remotePort})"
    val stSpan = NexusServerStreamTrekMark.begin(stream.guid)
    TeslaRequestFuture {
      try {
        var lastMessageTransmit: Future[Unit] = Promise[Unit]().success((): Unit).future

        var moreToGo = true
        while (moreToGo) {
          val parcel = stream.take
          if (parcel.status.isHeartbeat) {
            NexusServerReporter.onServerHeartbeat()
            transmitControlMessage(NexusStreamHeartbeatMsg(stream))

          } else if (parcel.status.isMarker) {
            NexusServerReporter.onServerStreamSucceed()
            val cstSpan = NexusServerCompleteSendTrekMark.begin(stream.guid)

            Await.ready(lastMessageTransmit, Duration.Inf)
            transmitControlMessage(NexusStreamCompleteMsg(stream, parcel.status)) onComplete {
              case Failure(t) =>
                log error burstStdMsg(s"$tag:$t", t)
                NexusServerStreamTrekMark.fail(stSpan)
                NexusServerReporter.onServerStreamFail()
              case Success(r) =>
                NexusServerCompleteSendTrekMark.end(cstSpan)
                NexusServerStreamTrekMark.end(stSpan)
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

            val psSpan = NexusServerParcelSendTrekMark.begin(stream.guid)
            lastMessageTransmit = transmitDataMessage(buffer, flush = false)
            lastMessageTransmit onComplete {
              case Failure(t) => log error burstStdMsg(s"$tag could not transmit parcel $t", t)
              case Success(r) => NexusServerParcelSendTrekMark.end(psSpan)
            }
            // this measurement is going to take almost no time since the write is async
            NexusServerReporter.onServerWrite(parcelSize, System.nanoTime() - writeStart)
          }
        }
      } catch safely {
        case t: Throwable =>
          NexusServerStreamTrekMark.fail(stSpan)
          NexusServerReporter.onServerStreamFail()
          log error burstStdMsg(s"$tag:$t", t)
      }
    }
  }
}
