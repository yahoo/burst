/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.server.connection

import io.netty.channel.Channel
import org.burstsys.nexus.NexusConnection
import org.burstsys.nexus.configuration.burstNexusPipeSizeProperty
import org.burstsys.nexus.message.{NexusStreamAbortMsg, NexusStreamInitiateMsg, NexusStreamInitiatedMsg, msgIds}
import org.burstsys.nexus.receiver.NexusServerMsgListener
import org.burstsys.nexus.server.{NexusServerListener, NexusStreamFeeder}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.transmitter.{NexusStreamHandler, NexusTransmitter}
import org.burstsys.nexus.trek.NexusServerStreamTrekMark
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

import java.util.concurrent.ConcurrentHashMap
import scala.util.{Failure, Success}

/**
 * This is the server side representative of a [[NexusConnection]]. These are found in burst-samplestore
 */
trait NexusServerConnection extends NexusConnection with NexusServerMsgListener {

  /**
   * The data feeder for this server connection
   *
   * @return
   */
  def feeder: NexusStreamFeeder

  /**
   * optional listener for the protocol
   *
   */
  def talksTo(listener: NexusServerListener): this.type

}

object NexusServerConnection {
  def apply(channel: Channel, transmitter: NexusTransmitter, feeder: NexusStreamFeeder = null): NexusServerConnection =
    NexusServerConnectionContext(channel: Channel, transmitter: NexusTransmitter, feeder: NexusStreamFeeder)
}

protected final case
class NexusServerConnectionContext(channel: Channel, transmitter: NexusTransmitter, feeder: NexusStreamFeeder)
  extends NexusServerConnection {

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  protected[this]
  var _listener: NexusServerListener = _

  private val _streamHandlers: ConcurrentHashMap[(VitalsUid, VitalsUid), NexusStreamHandler] = new ConcurrentHashMap()

  override def talksTo(listener: NexusServerListener): this.type = {
    _listener = listener
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // events
  ////////////////////////////////////////////////////////////////////////////////////

  override def onStreamInitiateMsg(request: NexusStreamInitiateMsg): Unit = {
    NexusServerStreamTrekMark.begin(request.guid, request.suid) { stage =>
      try {
        val pipe = TeslaParcelPipe(name = "nexus.server.stream", guid = request.guid, suid = request.suid, depth = burstNexusPipeSizeProperty.get).start
        val stream = NexusStream(connection = this, request.guid, request.suid, request, pipe).start
        log info s"NEXUS_STREAM_INITIATE NexusServerParcelHandler.initiateStream($link, ${msgIds(request)}) "
        transmitter.transmitControlMessage(NexusStreamInitiatedMsg(request, request.suid))

        val handler = NexusStreamHandler(stream, transmitter, feeder, stage)
        _streamHandlers.put(request.streamKey, handler)
        handler.start() andThen { case _ =>
          _streamHandlers.remove(request.streamKey)
        } andThen {
          case Success(_) =>
            NexusServerStreamTrekMark.end(stage)
          case Failure(t) =>
            log error(burstStdMsg(t), t)
            NexusServerStreamTrekMark.fail(stage, t)
        }

        if (_listener != null)
          _listener.onStreamInitiate(stream, request)

      } catch safely {
        case t: Throwable =>
          log error(burstStdMsg(t), t)
          throw t
      }
    }
  }


  override def onStreamAbortMsg(request: NexusStreamAbortMsg): Unit = {
    val handler = _streamHandlers.remove((request.guid, request.suid))
    if (handler != null) {
      handler.abort(request.status)
      if (_listener != null) {
        _listener.onStreamAbort(handler.stream, request)
      }
    }
  }


}
