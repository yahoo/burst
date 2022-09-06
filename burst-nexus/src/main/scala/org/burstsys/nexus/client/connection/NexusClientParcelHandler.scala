/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client.connection

import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.client.NexusClientReporter
import org.burstsys.nexus.message.{NexusMsg, NexusStreamAbortMsg, NexusStreamCompleteMsg, NexusStreamHeartbeatMsg, NexusStreamInitiateMsg, NexusStreamParcelMsg, msgIds}
import org.burstsys.nexus.stream.{NexusStream, newRuid, streamIds}
import org.burstsys.nexus.{NexusSliceKey, NexusStreamUid}
import org.burstsys.tesla.parcel.TeslaParcelStatus
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties.{BurstMotifFilter, VitalsPropertyMap}
import org.burstsys.vitals.uid._

import scala.concurrent.Promise

/**
 * client operations to support parcel streams
 */
trait NexusClientParcelHandler extends NexusClientConnection {

  self: NexusClientConnectionContext =>

  final override
  def startStream(
                   guid: VitalsUid, suid: NexusStreamUid, properties: VitalsPropertyMap, schema: BrioSchemaName, filter: BurstMotifFilter,
                   pipe: TeslaParcelPipe, sliceKey: NexusSliceKey, clientHostname: VitalsHostName, serverHostname: VitalsHostName
                 ): NexusStream = {
    val hdr = s"NexusClientParcelHandler.startStream($link, guid=$guid, suid=$suid)"
    if (isActive) {
      log warn s"$hdr already in a stream"
      throw VitalsException(s"$hdr already in a stream")
    }
    startMetrics()
    transmitter.transmitControlMessage(
      NexusStreamInitiateMsg(newRuid, guid, suid, properties, schema, filter, sliceKey, clientHostname, serverHostname)
    )
    _gate.lock()
    try {
      _promise = Promise[NexusStream]()
      _stream = NexusStream(connection = this, guid, suid, properties, schema, filter, pipe, sliceKey, clientHostname, serverHostname, _promise.future)
      val stream = _stream
      waitForStreamStart(stream)
      stream
    } finally _gate.unlock()
  }

  final override
  def onStreamParcelMsg(msg: NexusStreamParcelMsg): Unit = {
    val tag = s"NexusClientParcelHandler.onStreamParcelMsg(${msgHeader(msg)})"
    try {
      log info s"NexusStreamParcelMsg ${streamIds(_stream)} ${msgIds(msg)} count=${msg.parcel.bufferCount} action=receive"

      if (!msgOnThisStream(msg, "NexusClientParcelHandler.onStreamParcelMsg")) {
        return
      }

      if (_firstBatchNanos == 0) _firstBatchNanos = System.nanoTime()
      _batchCount add 1
      _itemCount add msg.parcel.bufferCount
      _byteCount add msg.parcel.inflatedSize


      NexusClientReporter.onParcelRead(msg.parcel.deflatedSize, msg.parcel.inflatedSize)
      if (_stream == null) {
        log warn burstStdMsg(s"$tag stream was null")
      } else {
        _stream put msg.parcel
      }
      if (_listener != null) {
        TeslaRequestFuture {
          _listener.onStreamParcel(msg)
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        _promise.failure(t)
    }
  }

  override
  def onStreamCompleteMsg(msg: NexusStreamCompleteMsg): Unit = {
    val tag = s"NexusClientParcelHandler.onStreamCompleteMsg(${msgHeader(msg)})"
    try {
      if (!msgOnThisStream(msg, "NexusClientParcelHandler.onStreamCompleteMsg")) {
        return
      }
      log info s"$tag ${msg.status} "
      _stream put msg.marker
      onStreamCompletion(msg, _stream)
    } finally _stream = null
  }

  override
  def abortStream(status: TeslaParcelStatus): Unit = {
    val tag = s"NexusClientParcelHandler.abortStream($link, status=$status)"
    _isStreamingData = false
    transmitter.transmitControlMessage(
      NexusStreamAbortMsg(_stream, status)
    )
  }

  final override
  def onStreamHeartbeatMsg(msg: NexusStreamHeartbeatMsg): Unit = {
    val tag = s"NexusClientParcelHandler.onStreamHeartbeatMsg(${msgHeader(msg)})"
    NexusClientReporter.onClientHeartbeat()
    try {
      if (!msgOnThisStream(msg, "NexusClientParcelHandler.onStreamHeartbeatMsg")) {
        return
      }

      if (_stream != null) {
        _stream put msg.marker
        if (_listener != null) {
          TeslaRequestFuture {
            _listener.onStreamHeartbeat(msg)
          }
        }
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        if (_promise != null)
          _promise.failure(t)
    }
  }

}
