/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.server.connection

import io.opentelemetry.api.common.AttributeKey
import org.burstsys.nexus.message.{NexusStreamCompleteMsg, NexusStreamHeartbeatMsg, NexusStreamParcelMsg, maxFrameLength}
import org.burstsys.nexus.server.connection.NexusStreamHandler._
import org.burstsys.nexus.server.{NexusServerReporter, NexusStreamFeeder}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.transceiver.NexusTransmitter
import org.burstsys.nexus.trek.{NexusServerCompleteSendTrekMark, NexusServerParcelSendTrekMark, NexusServerStreamTrekMark}
import org.burstsys.tesla
import org.burstsys.tesla.parcel
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.trek.TrekStage

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success}

object NexusStreamHandler {
  private val statusKey = AttributeKey.stringKey("stream.status")
  private val putItemCountKey = AttributeKey.longKey("stream.putItemCount")
  private val putBytesCountKey = AttributeKey.longKey("stream.putByteCount")
  private val itemCountKey = AttributeKey.longKey("stream.itemCount")
  private val expectedItemCountKey = AttributeKey.longKey("stream.expectedItemCount")
  private val potentialItemCountKey = AttributeKey.longKey("stream.potentialItemCount")
  private val rejectedItemCountKey = AttributeKey.longKey("stream.rejectedItemCount")

  private val parcelSizeKey = AttributeKey.longKey("parcel.size")
  private val parcelItemCountKey = AttributeKey.longKey("parcel.itemCount")
}

case class NexusStreamHandler(stream: NexusStream, transmitter: NexusTransmitter, feeder: NexusStreamFeeder, stage: TrekStage) {

  override def toString: String = s"NexusStreamHandler(stream=$stream  remote=${transmitter.remoteAddress})"

  def start(): Future[Unit] = {
    feeder.feedStream(stream)
    TeslaRequestFuture {
      var lastMessageTransmit: Future[Unit] = Promise.successful((): Unit).future
      try {
        var moreToGo = true
        while (moreToGo) {
          val parcel = stream.take
          if (parcel.status.isHeartbeat) {
            transmitHeartbeat()

          } else if (parcel.status.isMarker) {
            Await.ready(lastMessageTransmit, Duration.Inf)
            lastMessageTransmit = transmitStatusMessage(parcel)
            moreToGo = false

          } else if (parcel.currentUsedMemory > maxFrameLength) {
            dropParcel(parcel)

          } else {
            Await.ready(lastMessageTransmit, Duration.Inf)
            lastMessageTransmit = transmitDataParcel(parcel)
          }
        }
      } catch safely {
        case t: Throwable =>
          NexusServerReporter.onServerStreamFail()
          log error burstStdMsg(s"$this:$t", t)
          throw t
      }

      Await.result(lastMessageTransmit, Duration.Inf)
    }
  }

  def abort(status: parcel.TeslaParcelStatus): Unit = {
    feeder.abortStream(stream, status)
  }

  private def transmitHeartbeat(): Unit = {
    NexusServerReporter.onServerHeartbeat()
    stage.addEvent("Heartbeat")
    transmitter.transmitControlMessage(NexusStreamHeartbeatMsg(stream))
  }

  private def dropParcel(parcel: TeslaParcel): Unit = {
    log error s"$this parcel=$parcel size=${parcel.currentUsedMemory} exceeds maxFrameLength=$maxFrameLength, discarding parcel"
    stage.addEvent("Parcel dropped")
    NexusServerReporter.onServerDrop()
    tesla.parcel.factory releaseParcel parcel
  }

  private def transmitStatusMessage(parcel: TeslaParcel): Future[Unit] = {
    stage.addEvent("Stream complete: " + parcel.status.statusName)
    stage.span
      .setAttribute(statusKey, parcel.status.statusName)
      .setAttribute(putItemCountKey, stream.putItemCount.asInstanceOf[java.lang.Long])
      .setAttribute(putBytesCountKey, stream.putBytesCount.asInstanceOf[java.lang.Long])
      .setAttribute(itemCountKey, stream.itemCount.asInstanceOf[java.lang.Long])
      .setAttribute(expectedItemCountKey, stream.expectedItemCount.asInstanceOf[java.lang.Long])
      .setAttribute(potentialItemCountKey, stream.potentialItemCount.asInstanceOf[java.lang.Long])
      .setAttribute(rejectedItemCountKey, stream.rejectedItemCount.asInstanceOf[java.lang.Long])
    NexusServerCompleteSendTrekMark.begin(stream.guid) { stage =>
      transmitter.transmitControlMessage(NexusStreamCompleteMsg(stream, parcel.status)) andThen {
        case Failure(t) =>
          log error(burstStdMsg(s"$this:$t", t), t)
          NexusServerCompleteSendTrekMark.fail(stage, t)
          NexusServerStreamTrekMark.fail(stage, t)
          NexusServerReporter.onServerStreamFail()
        case Success(_) =>
          NexusServerCompleteSendTrekMark.end(stage)
          NexusServerStreamTrekMark.end(stage)
          NexusServerReporter.onServerStreamSucceed()
      }
    }

  }

  private def transmitDataParcel(parcel: TeslaParcel): Future[Unit] = {
    val writeStart = System.nanoTime
    val parcelSize = parcel.currentUsedMemory
    val future = NexusServerParcelSendTrekMark.begin(stream.guid) { stage =>
      log info s"NexusStreamParcelMsg guid=${stream.guid} suid=${stream.suid} count=${parcel.bufferCount} action=transmit"
      val update = NexusStreamParcelMsg(stream, parcel)
      stage.span
        .setAttribute(parcelItemCountKey, parcel.bufferCount)
        .setAttribute(parcelSizeKey, parcelSize)
      transmitter.transmitDataMessage(update) andThen {
        case Failure(t) =>
          NexusServerParcelSendTrekMark.fail(stage, t)
          log error burstStdMsg(s"$this could not transmit parcel $t", t)
        case Success(_) =>
          NexusServerParcelSendTrekMark.end(stage)
      }
    }
    // this measurement is going to take almost no time since the write is async
    NexusServerReporter.onServerWrite(parcelSize, System.nanoTime() - writeStart)
    future
  }
}
