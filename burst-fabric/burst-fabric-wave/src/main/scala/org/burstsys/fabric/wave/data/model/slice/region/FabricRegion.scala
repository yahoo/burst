/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region

import java.nio.file.Path

import org.burstsys.brio.blob.BrioBlob.BrioRegionIterator
import org.burstsys.brio.blob.BrioRegion
import org.burstsys.fabric.wave.configuration.cacheSpindleFolders
import org.burstsys.fabric.wave.data.model.slice.data.useHose
import org.burstsys.fabric.wave.data.model.slice.region.hose.{FabricRegionHose, FabricWriteMetrics}
import org.burstsys.fabric.wave.data.model.slice.region.reader.FabricRegionReader
import org.burstsys.fabric.wave.data.model.slice.region.writer.FabricRegionWriter
import org.burstsys.fabric.wave.data.model.snap.{FabricSnap, FabricSnapComponent}
import org.burstsys.tesla.parcel.TeslaParcel

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * a core bound partition of a slice associated with a single local file in the cache
 */
trait FabricRegion extends AnyRef
  with BrioRegion with FabricRegionIdentity with FabricSnapComponent with FabricWriteMetrics {

  /////////////////////////////////////////////////////////////
  // READS
  /////////////////////////////////////////////////////////////

  /**
   * final size of the readable file
   *
   * @return
   */
  def readFileSize: Long

  /**
   * open the region for reads
   *
   * @return
   */
  def loadRegionIntoMemory(): Unit

  /**
   * close the region which was open for reads
   *
   * @return
   */
  def evictRegionFromMemory(): Unit

  /**
   * delete a single region file
   */
  def flushRegionFromDisk(): Unit

  /////////////////////////////////////////////////////////////
  // WRITES
  /////////////////////////////////////////////////////////////

  /**
   * @return is the region empty
   */
  def regionIsRunt: Boolean

  /**
   * open the region for writes
   */
  def openRegionForWrites(): Unit

  /**
   * queue a parcel for the write subsystem to process
   *
   * @param parcel the parcel to write to the region
   */
  def queueParcelForWrite(parcel: TeslaParcel): Unit

  /**
   * indicate that all parcels have been queued for writing
   */
  def markAllParcelsQueued(): Unit

  /**
   * wait for all pending writes to complete
   */
  def waitForWritesToComplete(): Unit

  /**
   * close the region which was open for writes
   */
  def closeRegionForWrites(): Unit

}

object FabricRegion {

  def apply(snap: FabricSnap, regionIndex: Int, filePath: Path): FabricRegion = {
    val _regionTag: FabricRegionTag = {
      // TODO at some pt when we rewrite to be just a pump based system, we can do better here...
      cacheSpindleFolders.map {
        f => if (filePath.toString.startsWith(f)) f else null
      }.filter(_ != null).head
    }
    FabricRegionContext(
      snap = snap: FabricSnap,
      regionIndex: Int,
      _regionTag,
      filePath = filePath: Path
    )
  }

}

private final case
class FabricRegionContext(var snap: FabricSnap, regionIndex: Int, regionTag: FabricRegionTag, filePath: Path)
  extends FabricRegion {

  lazy val parameters = s"guid=${snap.guid}, regionIndex=${regionIndex}, file=${filePath}"

  override def toString: String = s"FabricRegion(regionIndex=$regionIndex regionTag=$regionTag filePath=$filePath)"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _regionReader = FabricRegionReader(snap, this, regionIndex, regionTag, filePath)

  private[this]
  val _regionWriter = FabricRegionWriter(snap, this, regionIndex, regionTag, filePath)

  private[this]
  val _regionHose = FabricRegionHose(snap, this, regionIndex, regionTag, filePath)

  private lazy val metricsSource: FabricWriteMetrics = if (useHose) _regionHose else _regionWriter

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def wireSnap(s: FabricSnap): Unit = {
    snap = s
    _regionReader.wireSnap(s)
    _regionWriter.wireSnap(s)
    _regionHose.wireSnap(s)
  }

  // ----------------------------------- READING -----------------------------------
  override
  def readFileSize: Long = _regionReader.readFileSize

  override
  def loadRegionIntoMemory(): Unit = {
    _regionReader.loadRegionIntoMemory()
  }

  override
  def evictRegionFromMemory(): Unit = {
    _regionReader.evictRegionFromMemory()
  }

  override
  def iterator: BrioRegionIterator = {
    _regionReader.iterator
  }

  override
  def flushRegionFromDisk(): Unit = {
    _regionReader.flushRegionFromDisk()
  }

  // ----------------------------------- WRITING -----------------------------------

  /**
   * this is only reliable if _regionWriter actually wrote the region. If the system shuts down before a runt region
   * is cleaned up, then this method will incorrectly return `false` when the snap is loaded from disk
   * @return is the region empty
   */
  override def regionIsRunt: Boolean = {
    if (useHose)
      _regionHose.regionIsRunt
    else
      _regionWriter.regionIsRunt
  }

  override def openRegionForWrites(): Unit = {
    if (useHose)
      _regionHose.open()
    else
      _regionWriter.openForWrites()
  }

  override def queueParcelForWrite(parcel: TeslaParcel): Unit = {
    if (useHose)
      _regionHose.putParcel(parcel)
    else
      _regionWriter queueParcelForWrite parcel
  }

  override def markAllParcelsQueued(): Unit = {
    if (useHose)
      Await.ready(_regionHose.close, Duration.Inf)
    else
      _regionWriter.allParcelsQueuedForWrite()
  }

  override def waitForWritesToComplete(): Unit = {
    if (useHose)
      ???
    else
      _regionWriter.waitForWritesToComplete()
  }

  override def closeRegionForWrites(): Unit = {
    if (useHose)
      Await.ready(_regionHose.close, Duration.Inf)
    else
      _regionWriter.closeForWrites()
  }

  // ----------------------------------- METRICS -----------------------------------

  override def parcelCount: Long =  metricsSource.parcelCount

  override def itemCount: Long = metricsSource.itemCount

  override def inflatedByteCount: Long = metricsSource.inflatedByteCount

  override def deflatedByteCount: Long =  metricsSource.deflatedByteCount

  override def syncWaitNs: Long =  metricsSource.syncWaitNs

  override def ioWaitNs: Long =  metricsSource.ioWaitNs

  override def elapsedNs: Long =  metricsSource.elapsedNs

}

