/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region.reader

import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.{Files, Path}
import org.burstsys.brio.blob.BrioBlob.BrioRegionIterator
import org.burstsys.fabric.wave.data.model.slice.region._
import org.burstsys.fabric.wave.data.model.snap.{FabricSnap, FabricSnapComponent}
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.offheap
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import sun.nio.ch.DirectBuffer

/**
 * read operations for a fabric region file
 */
trait FabricRegionReader extends FabricRegionIdentity with FabricSnapComponent {

  /**
   * The final read size of the region file
   *
   * @return
   */
  def readFileSize: Long

  /**
   * mmap memory ptr
   *
   * @return
   */
  def readMemoryPtr: TeslaMemoryPtr

  /**
   * The final iterable sequence of Brio Blobs
   * NOTE: this is the new version (sparkfree)
   *
   * @return
   */
  def iterator: BrioRegionIterator

  /**
   * remove region files from local disk cache
   */
  def flushRegionFromDisk(): Unit

  /**
   * open the region read subsystem
   */
  def loadRegionIntoMemory(): Unit

  /**
   * close the region read subsystem
   */
  def evictRegionFromMemory(): Unit

}

object FabricRegionReader {
  def apply(snap: FabricSnap, region: FabricRegion, regionIndex: Int, regionTag: FabricRegionTag, filePath: Path): FabricRegionReader =
    FabricRegionReaderContext(snap: FabricSnap, region: FabricRegion, regionIndex: Int, regionTag: FabricRegionTag, filePath: Path)
}

private[region] final case
class FabricRegionReaderContext(var snap: FabricSnap, region: FabricRegion, regionIndex: Int, regionTag: FabricRegionTag, filePath: Path)
  extends AnyRef with FabricRegionReader with FabricRegionAccessor {

  lazy val parameters = s"guid=${snap.guid}, regionIndex=${region.regionIndex}, file=${filePath}"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @transient private[this]
  var _file: RandomAccessFile = _

  @transient private[this]
  var _channel: FileChannel = _

  @transient private[this]
  var _readMappedByteBuffer: MappedByteBuffer = _

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def wireSnap(s: FabricSnap): Unit = snap = s

  override def readMemoryPtr: TeslaMemoryPtr = _readMappedByteBuffer.asInstanceOf[DirectBuffer].address

  override def readFileSize: Long = _file.length

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def loadRegionIntoMemory(): Unit = {
    lazy val tag = s"FabricRegionReader.loadIntoMemory($parameters)"
    synchronized {
      try {
        log info s"REGION_LOAD_INTO_MEMORY_START $tag"
        _file = new RandomAccessFile(filePath.toFile, "r")
        _channel = _file.getChannel
        _readMappedByteBuffer = _channel.map(FileChannel.MapMode.READ_ONLY, 0, _file.length)
        _readMappedByteBuffer.order(TeslaByteOrder)
      } catch safely {
        case t: Throwable =>
          val msg = s"REGION_LOAD_INTO_MEMORY_FAIL $t $tag"
          log error burstStdMsg(msg, t)
          throw VitalsException(msg, t)
      }

      val rm = readMagic
      val rv = readVersion

      if (rm != RegionMagic) {
        val msg = s"REGION_LOAD_BAD_MAGIC $RegionMagic but was $rm $tag "
        log error burstStdMsg(msg)
        throw VitalsException(msg)
      }

      if (rv != RegionVersion) {
        val msg = s"REGION_LOAD_BAD_VERSION $RegionVersion but was $rv $tag "
        log error burstStdMsg(msg)
        throw VitalsException(msg)
      }
      FabricRegionReporter.countReadOpen(_file.length)
    }
  }

  override
  def evictRegionFromMemory(): Unit = {
    lazy val tag = s"FabricRegionReader.evictFromMemory($parameters)"
    try {
      log info s"REGION_EVICT_START $tag"
      _readMappedByteBuffer.force()
      offheap.releaseBuffer(_readMappedByteBuffer)
      _readMappedByteBuffer = null
      _channel.close()
      _channel = null
      _file.close()
      _file = null
      FabricRegionReporter.countReadClose()
    } catch safely {
      case t: Throwable =>
        val msg = s"REGION_EVICT_FAIL $t $tag"
        log error burstStdMsg(msg, t)
        throw VitalsException(msg, t)
    }
  }

  override
  def flushRegionFromDisk(): Unit = {
    lazy val tag = s"FabricRegionReader.flushRegionFromDisk($parameters)"
    try {
      log info s"REGION_FLUSH $tag"
      if (Files.exists(filePath)) Files.delete(filePath)
    } catch safely {
      case t: Throwable =>
        val msg = s"REGION_FLUSH_FAIL $t $tag"
        log error burstStdMsg(msg, t)
        throw VitalsException(msg, t)
    }
  }


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * First byte is a magic number
   *
   * @return
   */
  private
  def readMagic: Byte = tesla.offheap.getByte(readMemoryPtr)

  /**
   * Second byte is a version 0-127
   *
   * @return
   */
  private
  def readVersion: Byte = tesla.offheap.getByte(readMemoryPtr + SizeOfRegionMagic)

}
