/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.stream

import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.{NexusConnection, NexusGlobalUid, NexusSliceKey, NexusStreamUid}
import org.burstsys.nexus.configuration.burstNexusStreamParcelPackerConcurrencyProperty
import org.burstsys.nexus.message.NexusStreamInitiateMsg
import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.packer.TeslaParcelPacker
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.parcel.{TeslaAbortMarkerParcel, TeslaEndMarkerParcel, TeslaExceptionMarkerParcel, TeslaHeartbeatMarkerParcel, TeslaNoDataMarkerParcel, TeslaParcel, TeslaTimeoutMarkerParcel}
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsPojo
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties.{BurstMotifFilter, VitalsPropertyMap, _}

import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * a data stream of parcels sent from server to client
 */
trait NexusStream extends VitalsService {

  override final val modality = VitalsPojo

  override final val serviceName = s"NexusStream($this)"

  /**
   * UID for global nexus operation for all streams
   */
  def guid: NexusGlobalUid

  /**
   * UID for a specific stream on a specific client/server connection
   */
  def suid: NexusStreamUid

  /**
   * properties associated with this stream
   */
  def properties: VitalsPropertyMap

  private lazy val extendedProperties = properties.extend

  /**
   * Get a property from the stream
   *
   * @param name the name of the property
   * @tparam T the type of the property
   * @return the value of the property, if defined. If the property is not defined throw an exception
   */
  def get[T: ClassTag](name: VitalsHostName): T = properties.getValueOrThrow[T](name)

  def getOrDefault[T: ClassTag](name: VitalsPropertyKey, defVal: T): T = extendedProperties.getValueOrDefault[T](name, defVal)

  /**
   * the schema for the data in the stream
   */
  def schema: BrioSchemaName

  /**
   */
  def filter: BurstMotifFilter

  /**
   * The number of items sent over the stream
   */
  def itemCount: Long

  def itemCount_=(items: Long): Unit

  /**
   * @return the number of items that the fabric store intends to send, based on sampling ratio or max data size
   */
  def expectedItemCount: Long

  def expectedItemCount_=(items: Long): Unit

  /**
   * @return The potential number of items the fabric store believes it could send, if all size and sampling constraints were lifted
   */
  def potentialItemCount: Long

  def potentialItemCount_=(items: Long): Unit

  /**
   * number of items rejected cause they exceeded next-item-max
   */
  def rejectedItemCount: Long

  def rejectedItemCount_=(items: Long): Unit

  /**
   * The FabricSliceKey equivalent in the context of NexusStream
   */
  def sliceKey: NexusSliceKey

  /**
   * The hostname of the client initiating the stream
   */
  def clientHostname: VitalsHostName

  /**
   * The hostname of the server to which the stream is initiated
   */
  def serverHostname: VitalsHostName

  /**
   * A future that will complete when all the stream data has been received
   */
  def completion: Future[NexusStream]

  /**
   * put a parcel on the stream.
   */
  def put(chunk: TeslaParcel): Unit

  /**
   * Hand off the buffer to be managed by parcel packer. This is the preferred method of putting data on the stream.
   */
  def put(buffer: TeslaMutableBuffer): Unit

  /**
   * take a parcel off of the stream.
   */
  def take: TeslaParcel

  def startHeartbeat(interval: Duration): Unit

  def stopHeartbeat(): Unit

  /**
   * Called by the server to mark the stream as complete and to send the appropriate signoff to the client
   *
   * @param itemCount          the number of items sent
   * @param expectedItemCount  the number of items we expected to send
   * @param potentialItemCount the number of items that exist in the dataset
   * @param rejectedItemCount  the number of items that failed to press
   */
  def complete(itemCount: Long, expectedItemCount: Long, potentialItemCount: Long, rejectedItemCount: Long, parcel: TeslaParcel = null): Unit

  /**
   * Called by the server to mark the stream as timed out, usually because pressing took too long, and send
   * the appropriate signoff to the client
   *
   * @param limit the timeout used by the server
   */
  def timedOut(limit: Duration): Unit

  /**
   * Called by the server to mark the stream as aborted
   *
   */
  def abort(): Unit

  /**
   * Called by the server to mark the stream as failed and to send the appropriate signoff to the client
   *
   */
  def completeExceptionally(exception: Throwable): Unit

}

object NexusStream {

  /**
   * Server-side constructor for a nexus stream. Parcel packing is enabled, and the stream has no receipt.
   */
  def apply(
             connection: NexusConnection,
             guid: NexusGlobalUid,
             suid: NexusStreamUid,
             initMsg: NexusStreamInitiateMsg,
             pipe: TeslaParcelPipe
           ): NexusStream =
    NexusStreamContext(
      connection,
      guid,
      suid,
      initMsg.properties,
      initMsg.schema,
      initMsg.filter,
      pipe,
      initMsg.sliceKey,
      initMsg.clientHostname,
      initMsg.serverHostname,
      outbound = true
    )

  /**
   * Client-side constructor for a nexus stream. Parcel packing is disabled.
   */
  def apply(
             connection: NexusConnection,
             guid: NexusGlobalUid,
             suid: NexusStreamUid,
             properties: VitalsPropertyMap,
             schema: BrioSchemaName,
             filter: BurstMotifFilter,
             pipe: TeslaParcelPipe,
             sliceKey: NexusSliceKey,
             clientHostname: VitalsHostName,
             serverHostname: VitalsHostName,
           ): NexusStream =
    NexusStreamContext(
      connection,
      guid,
      suid,
      properties,
      schema,
      filter,
      pipe,
      sliceKey,
      clientHostname,
      serverHostname,
      outbound = false
    )
}

private[stream] final case
class NexusStreamContext(
                          connection: NexusConnection,
                          guid: NexusGlobalUid,
                          suid: NexusStreamUid,

                          properties: VitalsPropertyMap,
                          schema: BrioSchemaName,
                          filter: BurstMotifFilter,
                          pipe: TeslaParcelPipe,

                          sliceKey: NexusSliceKey,
                          clientHostname: VitalsHostName,
                          serverHostname: VitalsHostName,

                          outbound: Boolean
                        ) extends NexusStream {

  override def toString: String = s"NexusStream(${connection.link} guid=$guid, suid=$suid)"

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private val _itemCount = new AtomicLong()

  private val _expectedItemCount = new AtomicLong()

  private val _potentialItemCount = new AtomicLong()

  private val _rejectedItemCount = new AtomicLong()

  private val _parcelPackers = ArrayBuffer[TeslaParcelPacker]()

  private var _parcelPackerConcurrency: Int = _

  private val _parcelPackerIndex = new AtomicInteger(0)

  private var heartbeat: Option[VitalsBackgroundFunction] = None

  private val _completion = Promise[NexusStream]()


  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle (use by nexus servers)
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  def start: this.type = {
    ensureNotRunning
    log debug startingMessage
    if (outbound) {
      _parcelPackerConcurrency = burstNexusStreamParcelPackerConcurrencyProperty.get
      if (_parcelPackerConcurrency > 0) {
        log debug burstStdMsg(s"Grabbing ${_parcelPackerConcurrency} parcel packers for $this")
        for (_ <- 0 until _parcelPackerConcurrency) {
          _parcelPackers.append(tesla.parcel.packer.grabPacker(guid, pipe))
        }
      }
    }
    markRunning
    this
  }

  override def stop: this.type = {
    log debug stoppingMessage

    if (outbound) {
      log debug burstStdMsg(s"Releasing ${_parcelPackerConcurrency} parcel packers for $this")
      drainParcelPackers()
      stopHeartbeat()
    }
    if (!_completion.isCompleted) {
      _completion.failure(VitalsException(s"$this was stopped before a finalizing method was called"))
    }

    markNotRunning
    this
  }

  private def drainParcelPackers(): Unit = {
    markNotRunning
    _parcelPackers.foreach(tesla.parcel.packer.releasePacker)
    _parcelPackers.clear()
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override def completion: Future[NexusStream] = _completion.future

  override def itemCount: Long = _itemCount.get

  override def itemCount_=(items: Long): Unit = _itemCount.set(items)

  override def expectedItemCount: Long = _expectedItemCount.get

  override def expectedItemCount_=(items: Long): Unit = _expectedItemCount.set(items)

  override def potentialItemCount: Long = _potentialItemCount.get

  override def potentialItemCount_=(items: Long): Unit = _potentialItemCount.set(items)

  override def rejectedItemCount: Long = _rejectedItemCount.get

  override def rejectedItemCount_=(items: Long): Unit = _rejectedItemCount.set(items)

  override def put(chunk: TeslaParcel): Unit = pipe put chunk

  override def put(buffer: TeslaMutableBuffer): Unit = {
    if (!outbound) {
      throw VitalsException("Only outbound streams should use #put(TeslaMutableBuffer)")
    }
    val nextPacker = _parcelPackerIndex.getAndSet((_parcelPackerIndex.get() + 1) % _parcelPackers.size)
    _parcelPackers(nextPacker).put(buffer)
  }

  override def take: TeslaParcel = pipe.take

  override def startHeartbeat(interval: Duration): Unit = {
    if (!outbound) {
      throw VitalsException("Only outbound streams should send heartbeats")
    }

    heartbeat = Some(new VitalsBackgroundFunction(s"$suid-heartbeat", 0 seconds, interval, {
      try {
        pipe.put(TeslaHeartbeatMarkerParcel)
      } catch safely {
        case t: Throwable =>
          log error(burstStdMsg(t), t)
      }
    }).start)
  }

  override def stopHeartbeat(): Unit = {
    heartbeat.foreach(_.stopIfNotAlreadyStopped)
  }

  override def complete(items: Long, expectedItems: Long, potentialItems: Long, rejectedItems: Long, status: TeslaParcel = null): Unit = {
    itemCount = items
    expectedItemCount = expectedItems
    potentialItemCount = potentialItems
    rejectedItemCount = rejectedItems

    _parcelPackers.foreach(_.finishWrites())
    pipe.put(
      if (status != null)
        status
      else if (items == 0)
        TeslaNoDataMarkerParcel
      else TeslaEndMarkerParcel
    )
    _completion.success(this)

    stop
  }

  override def timedOut(limit: Duration): Unit = {
    log debug s"$this failed to complete after $limit"
    if (outbound) {
      pipe.put(TeslaTimeoutMarkerParcel)
    }
    _completion.failure(new TimeoutException(s"$this failed to complete after $limit"))
    stop
  }

  override def abort(): Unit = {
    log debug s"$this aborted"
    if (outbound) {
      pipe.put(TeslaAbortMarkerParcel)
    }
    _completion.failure(VitalsException("Stream aborted"))
    stop
  }

  override def completeExceptionally(exception: Throwable): Unit = {
    log error(s"$this failed to complete", exception)
    if (outbound) {
      pipe.put(TeslaExceptionMarkerParcel)
    }
    _completion.failure(exception)
    stop
  }
}

