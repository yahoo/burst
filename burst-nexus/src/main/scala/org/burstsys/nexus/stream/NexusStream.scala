/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.stream

import java.util.concurrent.atomic.AtomicInteger
import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.configuration.burstNexusStreamParcelPackerConcurrencyProperty
import org.burstsys.nexus.message.NexusStreamInitiateMsg
import org.burstsys.nexus.{NexusConnection, NexusGlobalUid, NexusSliceKey, NexusStreamUid}
import org.burstsys.tesla.buffer.mutable.{TeslaMutableBuffer, endMarkerMutableBuffer}
import org.burstsys.tesla.parcel.packer.TeslaParcelPacker
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.parcel.{TeslaParcel, packer}
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsPojo
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties.{BurstMotifFilter, VitalsPropertyMap}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import org.burstsys.vitals.logging._

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
    * Hand off the buffer to be managed by parcel packer
    * Parcel packers are allocated on stream start and
    * released when [[endMarkerMutableBuffer]] is put on the stream
    */
  def put(buffer: TeslaMutableBuffer): Unit

  /**
    * take a parcel off of the stream.
    */
  def take: TeslaParcel

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
class NexusStreamContext(connection: NexusConnection, guid: NexusGlobalUid, suid: NexusStreamUid,
                         properties: VitalsPropertyMap, schema: BrioSchemaName, filter: BurstMotifFilter, pipe: TeslaParcelPipe,
                         sliceKey: NexusSliceKey, clientHostname: VitalsHostName, serverHostname: VitalsHostName,
                         receipt: Future[NexusStream], parcelPackingEnabled: Boolean) extends NexusStream {


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

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  def start: this.type = {
    ensureNotRunning
    //    log info startingMessage
    if (parcelPackingEnabled) {
      _parcelPackerConcurrency = burstNexusStreamParcelPackerConcurrencyProperty.getOrThrow
      if (_parcelPackerConcurrency > 0) {
        log info burstStdMsg(s"Grabbing ${_parcelPackerConcurrency} parcel packers for ${this}")
        for (_ <- 0 until _parcelPackerConcurrency) {
          _parcelPackers.append(packer.grabPacker(guid, pipe))
        }
      }
    }
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    //    log info stoppingMessage
    markNotRunning
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def put(chunk: TeslaParcel): Unit = pipe put chunk

  override
  def put(buffer: TeslaMutableBuffer): Unit = {
    if (buffer == endMarkerMutableBuffer) {
      log info burstStdMsg(s"Releasing ${_parcelPackerConcurrency} parcel packers for ${this}")
      _parcelPackers.foreach(packer.releasePacker)
    } else {
      _parcelPackers(_parcelPackerIndex.getAndSet((_parcelPackerIndex.get() + 1) % _parcelPackerConcurrency)).put(buffer)
    }
  }

  override
  def take: TeslaParcel = pipe.take

}

