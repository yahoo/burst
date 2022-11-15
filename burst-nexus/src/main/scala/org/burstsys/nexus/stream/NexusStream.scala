/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.stream

import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.NexusConnection
import org.burstsys.nexus.NexusGlobalUid
import org.burstsys.nexus.NexusSliceKey
import org.burstsys.nexus.NexusStreamUid
import org.burstsys.nexus.configuration.burstNexusStreamParcelPackerConcurrencyProperty
import org.burstsys.nexus.message.NexusStreamInitiateMsg
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.TeslaEndMarkerParcel
import org.burstsys.tesla.parcel.TeslaExceptionMarkerParcel
import org.burstsys.tesla.parcel.TeslaHeartbeatMarkerParcel
import org.burstsys.tesla.parcel.TeslaNoDataMarkerParcel
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.parcel.TeslaTimeoutMarkerParcel
import org.burstsys.tesla.parcel.packer
import org.burstsys.tesla.parcel.packer.TeslaParcelPacker
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsPojo
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties.BurstMotifFilter
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.properties._

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
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

  def rejectedItemCount_=(rItems: Long): Unit

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
  def receipt: Future[NexusStream]

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
  def complete(itemCount: Long, expectedItemCount: Long, potentialItemCount: Long, rejectedItemCount: Long): Unit

  /**
   * Called by the server to mark the stream as timed out, usually because pressing took too long, and send
   * the appropriate signoff to the client
   *
   * @param limit the timeout used by the server
   */
  def timedOut(limit: Duration): Unit

  /**
   * Called by the server to mark the stream as failed and to send the appropriate signoff to the client
   *
   * @param exception
   */
  def completeExceptionally(exception: Throwable): Unit

}

object NexusStream {

  /**
   * Server-side constructor for a nexus stream. Parcel packing is enabled, and the stream has no receipt.
   */
  def apply(connection: NexusConnection, guid: NexusGlobalUid, suid: NexusStreamUid,
            initMsg: NexusStreamInitiateMsg, pipe: TeslaParcelPipe): NexusStream =
    NexusStreamContext(connection, guid, suid, initMsg.properties, initMsg.schema, initMsg.filter, pipe,
      initMsg.sliceKey, initMsg.clientHostname, initMsg.serverHostname, receipt = null, parcelPackingEnabled = true)

  /**
   * Client-side constructor for a nexus stream. Parcel packing is disabled.
   */
  def apply(connection: NexusConnection, guid: NexusGlobalUid, suid: NexusStreamUid,
            properties: VitalsPropertyMap, schema: BrioSchemaName, filter: BurstMotifFilter, pipe: TeslaParcelPipe,
            sliceKey: NexusSliceKey, clientHostname: VitalsHostName, serverHostname: VitalsHostName,
            receipt: Future[NexusStream]): NexusStream =
    NexusStreamContext(connection, guid, suid, properties, schema, filter, pipe,
      sliceKey, clientHostname, serverHostname, receipt, parcelPackingEnabled = false)
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

                          receipt: Future[NexusStream],
                          parcelPackingEnabled: Boolean
                        ) extends NexusStream {


  var itemCount: Long = 0
  var expectedItemCount: Long = 0
  var potentialItemCount: Long = 0
  var rejectedItemCount: Long = 0

  override def toString: String = s"NexusStream(${connection.link} guid=$guid, suid=$suid)"

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _parcelPackers = ArrayBuffer[TeslaParcelPacker]()

  private[this]
  var _parcelPackerConcurrency: Int = _

  private[this]
  val _parcelPackerIndex = new AtomicInteger(0)

  private[this]
  var heartbeat: Option[VitalsBackgroundFunction] = None


  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  def start: this.type = {
    ensureNotRunning
    //    log info startingMessage
    if (parcelPackingEnabled) {
      _parcelPackerConcurrency = burstNexusStreamParcelPackerConcurrencyProperty.get
      if (_parcelPackerConcurrency > 0) {
        log info burstStdMsg(s"Grabbing ${_parcelPackerConcurrency} parcel packers for $this")
        for (_ <- 0 until _parcelPackerConcurrency) {
          _parcelPackers.append(packer.grabPacker(guid, pipe))
        }
      }
    }
    markRunning
    this
  }

  override def stop: this.type = {
    log info stoppingMessage

    log info burstStdMsg(s"Releasing ${_parcelPackerConcurrency} parcel packers for ${this}")
    drainParcelPackers()
    stopHeartbeat()

    markNotRunning
    this
  }

  private def drainParcelPackers(): Unit = {
    markNotRunning
    _parcelPackers.foreach(packer.releasePacker)
    _parcelPackers.clear()
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override def put(chunk: TeslaParcel): Unit = pipe put chunk

  override def put(buffer: TeslaMutableBuffer): Unit = {
    ensureRunning
    val nextPacker = _parcelPackerIndex.getAndSet((_parcelPackerIndex.get() + 1) % _parcelPackers.size)
    _parcelPackers(nextPacker).put(buffer)
  }

  override def take: TeslaParcel = pipe.take

  override def startHeartbeat(interval: Duration): Unit = {
    heartbeat = Some(new VitalsBackgroundFunction(s"$suid-heartbeat", 0 seconds, interval, {
      try {
        pipe.put(TeslaHeartbeatMarkerParcel)
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(t)
      }
    }).start)
  }

  override def stopHeartbeat(): Unit = {
    heartbeat.foreach(_.stopIfNotAlreadyStopped)
  }

  override def complete(items: Long, expectedItems: Long, potentialItems: Long, rejectedItems: Long): Unit = {
    itemCount = items
    expectedItemCount = expectedItems
    potentialItemCount = potentialItems
    rejectedItemCount = rejectedItems

    drainParcelPackers()
    val status = if (items == 0) TeslaNoDataMarkerParcel else TeslaEndMarkerParcel
    pipe.put(status)

    stop
  }

  override def timedOut(limit: Duration): Unit = {
    log info s"$this failed to complete after $limit"
    drainParcelPackers()
    pipe.put(TeslaTimeoutMarkerParcel)
    stop
  }

  override def completeExceptionally(exception: Throwable): Unit = {
    log error(s"$this failed to complete", exception)
    drainParcelPackers()
    pipe.put(TeslaExceptionMarkerParcel)
    stop
  }
}

