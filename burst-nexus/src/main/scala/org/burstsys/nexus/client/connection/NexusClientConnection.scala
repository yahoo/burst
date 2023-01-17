/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client.connection

import io.netty.channel.Channel
import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.client.{NexusClientListener, NexusClientReporter}
import org.burstsys.nexus.message.{NexusMsg, NexusStreamCompleteMsg, NexusStreamInitiatedMsg, msgIds}
import org.burstsys.nexus.receiver.NexusClientMsgListener
import org.burstsys.nexus.stream.{NexusStream, streamIds}
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
import java.util.concurrent.atomic.LongAdder
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
   * Start a stream
   */
  def startStream(guid: VitalsUid, suid: NexusStreamUid, properties: VitalsPropertyMap, schema: BrioSchemaName, filter: BurstMotifFilter,
                  pipe: TeslaParcelPipe, sliceKey: NexusSliceKey, clientHostname: VitalsHostName, serverHostname: VitalsHostName): NexusStream

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
  extends AnyRef with NexusClientConnection with NexusClientParcelHandler {

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  protected[this]
  val _gate: ReentrantLock = new ReentrantLock()

  protected[this]
  val _initiateResponded: Condition = _gate.newCondition()

  protected[this]
  var _isStreamingData: Boolean = false

  protected[this]
  var _startTime: Long = _

  protected[this]
  var _startNanos: Long = _

  protected[this]
  var _initiatedNanos: Long = _

  protected[this]
  var _firstBatchNanos: Long = _

  protected[this]
  var _lastProgressMs: Long = _

  protected[this]
  val _itemCount = new LongAdder

  protected[this]
  val _byteCount = new LongAdder

  protected[this]
  val _batchCount = new LongAdder

  protected[this]
  var _listener: NexusClientListener = _

  protected[this]
  var _promise: Promise[NexusStream] = _

  protected[this]
  var _stream: NexusStream = _

  protected def startMetrics(): Unit = {
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
    val sameStream = _stream.guid == msg.guid && _stream.suid == msg.suid
    if (!sameStream) {
      log warn s"CROSSED_NEXUS_STREAMS $tag received message from the wrong stream! msg=${msg.messageName} ${streamIds(_stream)} ${msgIds(msg)}"
    }
    sameStream
  }


  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def isActive: Boolean = _isStreamingData

  protected def waitForStreamStart(stream: NexusStream): Unit = {
    val span = NexusClientStreamStartTrekMark.begin(stream.guid, stream.suid)
    lazy val tag = s"NexusClientConnection.streamStart($transmitter, stream=$stream)"
    _gate.lock()
    var waitTimeout = false
    try {
      var waits = 0
      val waitStart = System.nanoTime()
      var waitElapsed = 0L
      var streamReady = false
      while (!isActive && !streamReady && !waitTimeout) {

        // check writable status
        streamReady = _initiateResponded.await(initiateWaitPeriodMs, TimeUnit.MILLISECONDS)

        waitElapsed = System.nanoTime - waitStart

        // check timeout status
        if (!streamReady && (waits < initiateMaxWaits)) {
          waits += 1
          log warn s"NEXUS_CLIENT_SLOW waits=$waits, maxWaits=$initiateMaxWaits, elapsed=$waitElapsed (${prettyTimeFromNanos(waitElapsed)}) $tag "
          NexusClientReporter.onClientSlow(waitElapsed)
          waitTimeout = false
        } else {
          waitTimeout = !streamReady
        }
      }

      if (waitTimeout) {
        NexusClientReporter.onClientTimeout(waitElapsed)
        val msg = s"NEXUS_CLIENT_TIMEOUT waits=$waits, maxWaits=$initiateMaxWaits, elapsed=$waitElapsed (${prettyTimeFromNanos(waitElapsed)}) $tag "
        NexusClientStreamStartTrekMark.fail(span)
        log error msg
        throw VitalsException(msg)
      }
      NexusClientStreamStartTrekMark.end(span)
    } catch safely {
      case t: Throwable =>
        NexusClientReporter.onClientStreamFail()
        NexusClientStreamStartTrekMark.fail(span)
        log error burstStdMsg(s"FAIL $t $tag", t)
        if (!_promise.isCompleted)
          _promise.failure(t)
        throw t
    } finally {
      if (!waitTimeout) {
        log info s"NEXUS_STREAM_STARTED elapsed=${System.nanoTime - _startNanos} (${prettyTimeFromNanos(System.nanoTime - _startNanos)}) $tag"
      }
      _gate.unlock()
    }
  }

  override
  def onStreamInitiatedMsg(msg: NexusStreamInitiatedMsg): Unit = {
    lazy val hdr = s"NexusClientConnection.onStreamInitiatedResponse($link, ${msgIds(msg)})"
    try {
      log info s"NEXUS_STREAM_INITATE_RESPONSE $hdr"
      _gate.lock()
      try {
        if (!msgOnThisStream(msg, "NexusClientConnection.onStreamInitiatedResponse")) {
          return
        }

        if (isActive) {
          log warn s"$hdr already in stream"
          throw VitalsException(s"NEXUS_ALREADY_IN_STREAM $hdr")
        }
        _isStreamingData = true
        _initiateResponded.signalAll()
      } finally _gate.unlock()
      if (_listener != null) {
        TeslaRequestFuture {
          _listener.onStreamInitiated(msg)
        }
      }
      _initiatedNanos = System.nanoTime()
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"$hdr", t)
        if (!_promise.isCompleted)
          _promise.failure(t)
    }
  }

  protected
  def onStreamCompletion(update: NexusStreamCompleteMsg, stream: NexusStream): Unit = {
    lazy val tag = s"NexusClientConnection.onCompletion($link, ${msgIds(update)})"
    val span = NexusClientStreamTerminateTrekMark.begin(stream.guid, stream.suid)
    try {
      stream.itemCount = update.itemCount
      stream.expectedItemCount = update.expectedItemCount
      stream.potentialItemCount = update.potentialItemCount
      stream.rejectedItemCount = update.rejectedItemCount

      if (_itemCount.longValue != update.itemCount)
        log warn s"SEND_RECEIVE_MISMATCH $stream did not receive expected number of items! sentItems=${update.itemCount} receivedItems=${_itemCount.longValue} $tag"

      if (_listener != null) {
        TeslaRequestFuture {
          _listener.onStreamComplete(update)
        }
      }
      val elapsedNanos = System.nanoTime() - _startNanos
      val endTime = System.currentTimeMillis()
      val itemSize = if (_itemCount.longValue == 0) 0 else _byteCount.longValue / _itemCount.longValue
      val batchSize = if (_batchCount.longValue == 0) 0 else _byteCount.longValue / _batchCount.longValue
      log info
        s"""
           |$tag STREAM RECV END ,
           |  startTime=${_startTime} (${prettyDateTimeFromMillis(_startTime)}) ,
           |  endTime=$endTime (${prettyDateTimeFromMillis(endTime)}) ,
           |  initiatedNanos=${_initiatedNanos - _startNanos} (${prettyTimeFromNanos(_initiatedNanos - _startNanos)}) ,
           |  firstBatchNanos=${_firstBatchNanos - _startNanos} (${prettyTimeFromNanos(_firstBatchNanos - _startNanos)}) ,
           |  finishedNanos=$elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}) ,
           |  itemCount=${_itemCount} (${prettyFixedNumber(_itemCount.longValue)}) ,
           |  potentialItemCount=${stream.potentialItemCount} (${prettyFixedNumber(stream.potentialItemCount)})
           |  reportedItemCount=${update.itemCount} (${prettyFixedNumber(update.itemCount)}) ,
           |  reportedPotentialItemCount=${update.potentialItemCount} (${prettyFixedNumber(update.potentialItemCount)}) ,
           |  reportedRejectedItemCount=${update.rejectedItemCount} (${prettyFixedNumber(update.rejectedItemCount)}) ,
           |  byteCount=${_byteCount} (${prettyByteSizeString(_byteCount.longValue)}) ,
           |  batchCount=${_batchCount} ,
           |  itemSize=$itemSize (${prettyByteSizeString(itemSize)}) ,
           |  batchSize=$batchSize (${prettyByteSizeString(batchSize)}) ,
           |  ${prettyRateString("byte", _byteCount.longValue, elapsedNanos)} ,
           """.stripMargin
      _isStreamingData = false
      if (!_promise.isCompleted) {
        log info s"PROMISE_SUCCESS $tag "
        _promise.success(stream)
      } else {
        log info s"PROMISE_HAD_FAILED_PREVIOUSLY $tag"
      }
      NexusClientReporter.onClientStreamSucceed()
      NexusClientStreamTerminateTrekMark.end(span)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"FAIL $t $tag", t)
        NexusClientReporter.onClientStreamFail()
        NexusClientStreamTerminateTrekMark.fail(span)
        if (!_promise.isCompleted)
          _promise.failure(t)
        // clear this out
        _isStreamingData = false
    }
  }

}
