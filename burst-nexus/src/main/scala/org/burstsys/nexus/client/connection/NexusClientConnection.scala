/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client.connection

import io.netty.channel.Channel
import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.client.{NexusClientListener, NexusClientReporter}
import org.burstsys.nexus.message.{NexusMsg, NexusStreamAbortMsg, NexusStreamCompleteMsg, NexusStreamHeartbeatMsg, NexusStreamInitiateMsg, NexusStreamInitiatedMsg, NexusStreamParcelMsg, msgIds}
import org.burstsys.nexus.receiver.NexusClientMsgListener
import org.burstsys.nexus.stream.{NexusStream, newRuid, streamIds}
import org.burstsys.nexus.transmitter.NexusTransmitter
import org.burstsys.nexus.trek.{NexusClientStreamStartTrekMark, NexusClientStreamTerminateTrekMark}
import org.burstsys.nexus.{NexusConnection, NexusSliceKey, NexusStreamUid}
import org.burstsys.tesla.parcel.TeslaParcelStatus
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties.{BurstMotifFilter, VitalsPropertyMap}
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.uid._

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicBoolean, LongAdder}
import java.util.concurrent.locks.{Condition, ReentrantLock}
import scala.concurrent.Promise
import scala.language.postfixOps

/**
 * This is the client side representative of a [[NexusConnection]].
 * Burst cell workers have [[NexusClientConnection]]s to connect to datastores and load data.
 */
trait NexusClientConnection extends NexusConnection with NexusClientMsgListener {

  /**
   * Is the connection in active use by someone
   *
   * @return
   */
  def isActive: Boolean

  /**
   * optional listener for the protocol
   */
  def talksTo(listener: NexusClientListener): this.type

  /**
   * Start a stream, blocking until the nexus server connects
   */
  def startStream(
                   guid: VitalsUid,
                   suid: NexusStreamUid,
                   properties: VitalsPropertyMap,
                   schema: BrioSchemaName,
                   filter: BurstMotifFilter,
                   pipe: TeslaParcelPipe,
                   sliceKey: NexusSliceKey,
                   clientHostname: VitalsHostName,
                   serverHostname: VitalsHostName
                 ): NexusStream

  /**
   * abort the current stream on this connection with a specific [[TeslaParcelStatus]]
   */
  def abortStream(status: TeslaParcelStatus): Unit

}

object NexusClientConnection {

  def apply(channel: Channel, transmitter: NexusTransmitter): NexusClientConnection =
    NexusClientConnectionContext(channel: Channel, transmitter: NexusTransmitter)

}

protected final case
class NexusClientConnectionContext(channel: Channel, transmitter: NexusTransmitter)
  extends NexusClientConnection {

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private val _gate: ReentrantLock = new ReentrantLock()

  private val _initiateReceived: Condition = _gate.newCondition()

  private val _isStreamingData = new AtomicBoolean(false)

  private var _startTime: Long = _

  private var _startNanos: Long = _

  private var _initiatedNanos: Long = _

  private var _firstBatchNanos: Long = _

  private var _lastProgressMs: Long = _

  private val _itemCount = new LongAdder

  private val _byteCount = new LongAdder

  private val _batchCount = new LongAdder

  private var _listener: NexusClientListener = _

  private var _stream: NexusStream = _

  private def startMetrics(): Unit = {
    _startTime = System.currentTimeMillis()
    _startNanos = System.nanoTime()
    _initiatedNanos = 0
    _firstBatchNanos = 0
    _lastProgressMs = 0
    _itemCount.reset()
    _byteCount.reset()
    _batchCount.reset()
  }

  override def talksTo(listener: NexusClientListener): this.type = {
    _listener = listener
    this
  }

  protected def msgHeader(msg: NexusMsg): String = s"$link, ${streamIds(_stream)} ${msgIds(msg)}"

  protected def msgOnThisStream(msg: NexusMsg, tag: String): Boolean = {
    if (_stream == null) {
      log warn s"CLOSED_NEXUS_STREAM $tag received message on a closed stream! msg=${msg.messageName} ${msgIds(msg)}"
      return false
    }
    val sameStream = _stream.guid == msg.guid && _stream.suid == msg.suid
    if (!sameStream) {
      log warn s"CROSSED_NEXUS_STREAMS $tag received message from the wrong stream! msg=${msg.messageName} ${streamIds(_stream)} ${msgIds(msg)}"
    }
    sameStream
  }


  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override def isActive: Boolean = _isStreamingData.get

  override def startStream(
                            guid: VitalsUid, suid: NexusStreamUid, properties: VitalsPropertyMap, schema: BrioSchemaName, filter: BurstMotifFilter,
                            pipe: TeslaParcelPipe, sliceKey: NexusSliceKey, clientHostname: VitalsHostName, serverHostname: VitalsHostName
                          ): NexusStream = {
    val hdr = s"NexusClientConnection.startStream($link, guid=$guid, suid=$suid)"
    if (isActive) {
      log warn s"$hdr already in a stream"
      throw VitalsException(s"$hdr already in a stream")
    }
    startMetrics()
    log info s"$hdr"
    transmitter.transmitControlMessage(
      NexusStreamInitiateMsg(newRuid, guid, suid, properties, schema, filter, sliceKey, clientHostname, serverHostname)
    )

    _gate.lock()
    try {
      _stream = NexusStream(connection = this, guid, suid, properties, schema, filter, pipe, sliceKey, clientHostname, serverHostname)
      waitForStreamStart(_stream)
      _stream
    } finally _gate.unlock()
  }

  private def waitForStreamStart(stream: NexusStream): Unit = {
    val span = NexusClientStreamStartTrekMark.begin(stream.guid, stream.suid)
    lazy val tag = s"NexusClientConnection.streamStart($transmitter, stream=$stream)"
    var waitTimeout = false

    _gate.lock()
    try {
      var waits = 0
      var waitElapsed = 0L
      val waitStart = System.nanoTime()
      while (!(isActive || waitTimeout)) {

        // wait for server connection
        _initiateReceived.await(initiateWaitPeriodMs, TimeUnit.MILLISECONDS)
        waitElapsed = System.nanoTime - waitStart

        // check timeout status
        if (!isActive && (waits < initiateMaxWaits)) {
          waits += 1
          log debug s"NEXUS_CLIENT_SLOW waits=$waits, maxWaits=$initiateMaxWaits, elapsed=$waitElapsed (${prettyTimeFromNanos(waitElapsed)}) $tag "
          NexusClientReporter.onClientSlow(waitElapsed)
        } else {
          waitTimeout = !isActive
        }
      }

      if (waitTimeout) {
        NexusClientReporter.onClientTimeout(waitElapsed)
        val msg = s"NEXUS_CLIENT_TIMEOUT waits=$waits, maxWaits=$initiateMaxWaits, elapsed=$waitElapsed (${prettyTimeFromNanos(waitElapsed)}) $tag "
        NexusClientStreamStartTrekMark.fail(span)
        log error msg
        throw VitalsException(msg)
      } else {
        log debug s"NEXUS_STREAM_STARTED elapsed=${System.nanoTime - _startNanos} (${prettyTimeFromNanos(System.nanoTime - _startNanos)}) $tag"
      }
      NexusClientStreamStartTrekMark.end(span)

    } catch safely {
      case t: Throwable =>
        NexusClientReporter.onClientStreamFail()
        NexusClientStreamStartTrekMark.fail(span)
        log error burstStdMsg(s"FAIL $t $tag", t)
        _stream.completeExceptionally(t)
        throw t
    } finally _gate.unlock()
  }

  override def onStreamInitiatedMsg(msg: NexusStreamInitiatedMsg): Unit = {
    lazy val hdr = s"NexusClientConnection.onStreamInitiatedResponse($link, ${msgIds(msg)})"
    try {
      log debug s"NEXUS_STREAM_INITIATE_RESPONSE $hdr"
      _gate.lock()
      try {
        if (!msgOnThisStream(msg, "NexusClientConnection.onStreamInitiatedResponse")) {
          return
        }

        if (isActive) {
          log warn s"$hdr already in stream"
          throw VitalsException(s"NEXUS_ALREADY_IN_STREAM $hdr")
        }
        _isStreamingData.set(true)
        _initiateReceived.signalAll()
      } finally _gate.unlock()

      forward(_.onStreamInitiated(msg))
      _initiatedNanos = System.nanoTime()
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"$hdr", t)
        _stream.completeExceptionally(t)
    }
  }

  override def onStreamParcelMsg(msg: NexusStreamParcelMsg): Unit = {
    val tag = s"NexusClientConnection.onStreamParcelMsg(${msgHeader(msg)})"
    try {
      log debug s"NexusStreamParcelMsg ${streamIds(_stream)} ${msgIds(msg)} count=${msg.parcel.bufferCount} action=receive"

      if (!msgOnThisStream(msg, "NexusClientConnection.onStreamParcelMsg")) {
        return
      }

      if (_firstBatchNanos == 0) {
        _firstBatchNanos = System.nanoTime()
      }
      _batchCount add 1
      _itemCount add msg.parcel.bufferCount
      _byteCount add msg.parcel.inflatedSize


      NexusClientReporter.onParcelRead(msg.parcel.deflatedSize, msg.parcel.inflatedSize)

      if (_stream == null) {
        log warn burstStdMsg(s"$tag stream was null")
      } else {
        _stream put msg.parcel
      }
      forward(_.onStreamParcel(msg))

    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"NEXUS_PARCEL_FAIL $tag", t)
        _stream.completeExceptionally(t)
    }
  }

  override def onStreamCompleteMsg(msg: NexusStreamCompleteMsg): Unit = {
    val tag = s"NexusClientConnection.onStreamCompleteMsg(${msgHeader(msg)})"
    if (!msgOnThisStream(msg, "NexusClientConnection.onStreamCompleteMsg")) {
      return
    }
    try {
      log info s"$tag ${msg.status} items=${msg.itemCount} expected=${msg.expectedItemCount} potential=${msg.potentialItemCount} rejected=${msg.rejectedItemCount}"
      val span = NexusClientStreamTerminateTrekMark.begin(_stream.guid, _stream.suid)
      try {
        if (_itemCount.longValue != msg.itemCount) {
          log warn s"SEND_RECEIVE_MISMATCH ${_stream} did not receive expected number of items! sentItems=${msg.itemCount} receivedItems=${_itemCount.longValue} $tag"
        }

        val elapsedNanos = System.nanoTime() - _startNanos
        val endTime = System.currentTimeMillis()
        val itemSize = if (_itemCount.longValue == 0) 0 else _byteCount.longValue / _itemCount.longValue
        val batchSize = if (_batchCount.longValue == 0) 0 else _byteCount.longValue / _batchCount.longValue
        // TODO: map this to metrics
        log debug
          s"""$tag STREAM RECV END ,
             |  startTime=${_startTime} (${prettyDateTimeFromMillis(_startTime)}) ,
             |  endTime=$endTime (${prettyDateTimeFromMillis(endTime)}) ,
             |  initiatedNanos=${_initiatedNanos - _startNanos} (${prettyTimeFromNanos(_initiatedNanos - _startNanos)}) ,
             |  firstBatchNanos=${_firstBatchNanos - _startNanos} (${prettyTimeFromNanos(_firstBatchNanos - _startNanos)}) ,
             |  finishedNanos=$elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}) ,
             |  itemCount=${_itemCount} (${prettyFixedNumber(_itemCount.longValue)}) ,
             |  potentialItemCount=${_stream.potentialItemCount} (${prettyFixedNumber(_stream.potentialItemCount)})
             |  reportedItemCount=${msg.itemCount} (${prettyFixedNumber(msg.itemCount)}) ,
             |  reportedPotentialItemCount=${msg.potentialItemCount} (${prettyFixedNumber(msg.potentialItemCount)}) ,
             |  reportedRejectedItemCount=${msg.rejectedItemCount} (${prettyFixedNumber(msg.rejectedItemCount)}) ,
             |  byteCount=${_byteCount} (${prettyByteSizeString(_byteCount.longValue)}) ,
             |  batchCount=${_batchCount} ,
             |  itemSize=$itemSize (${prettyByteSizeString(itemSize)}) ,
             |  batchSize=$batchSize (${prettyByteSizeString(batchSize)}) ,
             |  ${prettyRateString("byte", _byteCount.longValue, elapsedNanos)} ,
   """.stripMargin

        _isStreamingData.set(false)
        _stream.complete(msg.itemCount, msg.expectedItemCount, msg.potentialItemCount, msg.rejectedItemCount, msg.marker)
        forward(_.onStreamComplete(msg))

        NexusClientReporter.onClientStreamSucceed()
        NexusClientStreamTerminateTrekMark.end(span)
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(s"NEXUS_STREAM_COMPLETE_FAIL $tag", t)
          NexusClientReporter.onClientStreamFail()
          NexusClientStreamTerminateTrekMark.fail(span)
          _stream.completeExceptionally(t)
          _isStreamingData.set(false)
      }
    } finally _stream = null
  }

  override def abortStream(status: TeslaParcelStatus): Unit = {
    _isStreamingData.set(false)
    transmitter.transmitControlMessage(
      NexusStreamAbortMsg(_stream, status)
    )
  }

  override def onStreamHeartbeatMsg(msg: NexusStreamHeartbeatMsg): Unit = {
    val tag = s"NexusClientConnection.onStreamHeartbeatMsg(${msgHeader(msg)})"
    NexusClientReporter.onClientHeartbeat()
    try {
      if (!msgOnThisStream(msg, "NexusClientConnection.onStreamHeartbeatMsg")) {
        return
      }

      if (_stream != null) {
        _stream put msg.marker
        forward(_.onStreamHeartbeat(msg))
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"NEXUS_HEARTBEAT_FAIL $tag", t)
        _stream.completeExceptionally(t)
    }
  }

  private def forward(action: NexusClientListener => Unit): Unit = {
    if (_listener != null) {
      TeslaRequestFuture {
        action(_listener)
      }
    }
  }
}
