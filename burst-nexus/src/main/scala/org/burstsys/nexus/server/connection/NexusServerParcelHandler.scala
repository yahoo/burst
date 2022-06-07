/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.server.connection

import org.burstsys.nexus.configuration.burstNexusPipeSizeProperty
import org.burstsys.nexus.message.{NexusStreamAbortMsg, NexusStreamInitiateMsg, NexusStreamInitiatedMsg, msgIds}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.{NexusGlobalUid, NexusStreamUid}
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe

/**
  * nexus server parcel stream functionality
  */
trait NexusServerParcelHandler extends AnyRef {

  self: NexusServerConnectionContext =>

  private[this]
  var _stream: NexusStream = _

  protected final
  def initiateStream(initiateMsg: NexusStreamInitiateMsg, guid: NexusGlobalUid, suid: NexusStreamUid): Unit = {
    val pipe = TeslaParcelPipe(name = "nexus.server.stream", guid = guid, suid = suid, depth = burstNexusPipeSizeProperty.getOrThrow).start
    _stream = NexusStream(connection = this, guid, suid, initiateMsg, pipe)
    log info s"NEXUS_STREAM_INITIATE NexusServerParcelHandler.initiateStream($link, ${msgIds(initiateMsg)}) "
    transmitter.transmitControlMessage(NexusStreamInitiatedMsg(initiateMsg, suid))
    _inStream = true
    transmitter.transmitDataStream(_stream)
    if (feeder != null)
      feeder.feedStream(_stream.start)
    if (_listener != null)
      _listener.onStreamInitiate(_stream, initiateMsg)
  }

  final override
  def onStreamAbortMsg(request: NexusStreamAbortMsg): Unit = {
    lazy val hdr = s"NexusServerParcelHandler.onStreamAbortMsg($link, ${msgIds(request)})"
    if (feeder != null)
      feeder.abortStream(_stream, request.status)
    if (_listener != null)
      _listener.onStreamAbort(_stream, request)
  }

}
