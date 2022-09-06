/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.transmitter

import org.burstsys.nexus.message.{NexusStreamCompleteMsg, NexusStreamHeartbeatMsg, NexusStreamParcelMsg, maxFrameLength}
import org.burstsys.nexus.server.NexusServerReporter
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.trek.{NexusServerCompleteSendTrekMark, NexusServerParcelSendTrekMark, NexusServerStreamTrekMark}
import org.burstsys.tesla
import org.burstsys.tesla.thread
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import org.burstsys.vitals.logging._

/**
 * data parcel stream transmit functionality
 */
trait NexusParcelTransmitter {

  self: NexusTransmitter =>

  final val slowSync = 1 seconds

  final
  def transmitDataStream(stream: NexusStream): Unit = {
    lazy val tag = s"NexusParcelTransmitter.transmitDataStream(id=$id, $link, stream=$stream  ${remoteAddress}:${remotePort})"
    TeslaRequestFuture {
      try {
        var dataFuture: Future[Unit] = Promise[Unit]().success((): Unit).future

        NexusServerStreamTrekMark.begin(stream.guid)
        var moreToGo = true
        while (moreToGo) {
          val parcel = stream.take
          if (parcel.status.isHeartbeat) {
            NexusServerReporter.onServerHeartbeat()
            transmitControlMessage(NexusStreamHeartbeatMsg(stream))

          } else if (parcel.status.isMarker) {
            NexusServerReporter.onServerStreamSucceed()
            NexusServerCompleteSendTrekMark.begin(stream.guid)

            Await.ready(dataFuture, Duration.Inf)
            transmitControlMessage(NexusStreamCompleteMsg(stream, parcel.status)) onComplete {
              case Failure(t) =>
                log error burstStdMsg(s"$tag:$t", t)
                NexusServerStreamTrekMark.fail(stream.guid)
                NexusServerReporter.onServerStreamFail()
              case Success(r) =>
                NexusServerCompleteSendTrekMark.end(stream.guid)
                NexusServerStreamTrekMark.end(stream.guid)
            }
            moreToGo = false

          } else if (parcel.currentUsedMemory > maxFrameLength) {
            log error s"$tag parcel=$parcel size=${parcel.currentUsedMemory} exceeds maxFrameLength=$maxFrameLength, discarding parcel"
            NexusServerReporter.onServerDrop()
            tesla.parcel.factory releaseParcel parcel

          } else {
            Await.ready(dataFuture, Duration.Inf)

            log info s"NexusStreamParcelMsg guid=${stream.guid} suid=${stream.suid} count=${parcel.bufferCount} action=transmit"
            val writeStart = System.nanoTime
            val parcelSize = parcel.currentUsedMemory
            val update = NexusStreamParcelMsg(stream, parcel)
            val buffer = channel.alloc().buffer(parcel.currentUsedMemory)
            update.encode(buffer)
            tesla.parcel.factory releaseParcel parcel

            NexusServerParcelSendTrekMark.begin(stream.guid)
            dataFuture = transmitDataMessage(buffer, flush = false)
            dataFuture onComplete {
              case Failure(t) => log error burstStdMsg(s"$tag could not transmit parcel $t", t)
              case Success(r) => NexusServerParcelSendTrekMark.end(stream.guid)
            }
            // this measurement is going to take almost no time since the write is async
            NexusServerReporter.onServerWrite(parcelSize, System.nanoTime() - writeStart)
          }
        }
      } catch safely {
        case t: Throwable =>
          NexusServerStreamTrekMark.fail(stream.guid)
          NexusServerReporter.onServerStreamFail()
          log error burstStdMsg(s"$tag:$t", t)
      }
    }
  }
}
