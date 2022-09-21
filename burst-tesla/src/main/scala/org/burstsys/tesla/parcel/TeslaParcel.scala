/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel

import java.nio.ByteBuffer

import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.buffer.TeslaBuffer
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.internal.{TeslaParcelBulk, TeslaParcelDeflator, TeslaParcelInflator, TeslaParcelReader, TeslaParcelWriter}
import org.burstsys.tesla.parcel.state.TeslaParcelState
import org.burstsys.tesla.part.TeslaPart
import org.burstsys.tesla.pool.TeslaPoolId

import scala.language.implicitConversions

/**
  * A [[TeslaParcel]] is a native memory hosted
  * batch of [[TeslaBuffer]] that can be compressed (deflated) or uncompressed (inflated).
  * These are allocated on top of a [[org.burstsys.tesla.block.TeslaBlock]]
  *
  * ===binary image===
  * The parcel's binary image two forms - compressed and uncompressed - the compressed format is
  * snappy (currently). The uncompressed form matches exactly the Fabric Region form
  * so that the uncompressed binary image can be written directly to the disk in a single
  * [[sun.nio.ch.DirectBuffer]] NIO Async op without any additional copies. The only copy
  * at the end of the pipeline is a native to native memory copy as the decompression op
  * is executed. The disk image is uncompressed so that we can mmap it directly into memory
  * and scan.
  */
trait TeslaParcel extends Any with TeslaBlockPart with  TeslaParcelStatusMarker {

  //////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
    * @return The id of the pool for this parcel in the factory
    */
  def poolId: TeslaPoolId

  /**
    * @return the total memory in this parcel useable for buffer storage
    */
  def maxAvailableMemory: TeslaMemorySize

  /**
    * @return the amount of memory currently in the parcel by buffers
    */
  def currentUsedMemory: TeslaMemorySize

  /**
    * @return the offset of the parcel's first buffer
    */
  def bufferSlotsStart: TeslaMemoryOffset

  /**
    * the underlying block ptr which contains this parcel
    *
    * @return
    */
//  val blockPtr: TeslaMemoryPtr TODO SCALA 2.12 does not allow this

  /**
    * the pointer to the start of the buffer slot data
    *
    * @return
    */
  def bufferSlotsStartPtr: TeslaMemoryPtr

  /**
    * the size of the header
    *
    * @return
    */
  def headerSize: TeslaMemoryOffset

  /**
    * the number of buffers in this parcel
    *
    * @return
    */
  def bufferCount: Int

  /**
    * the size
    *
    * @return
    */
  def inflatedSize: Int

  /**
    * the deflated (snappy) size of the contents
    *
    * @return
    */
  def deflatedSize: Int

  /**
    * is the underlying parcel inflated?
    *
    * @return
    */
  def isInflated: Boolean

  /**
    * offset for next buffer to write into
    *
    * @return
    */
  def nextSlotOffset: TeslaMemoryOffset

  //////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
    * initialize this reusable part
    *
    */
  def initialize(id: TeslaPoolId): Unit

  /**
    * reset this reusable part
    *
    * @return
    */
  def reset: TeslaParcel

  //////////////////////////////////////////////////////////////////////////////////////////
  // compression/decompression
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
    * decompress this parcel into a given native memory location.
    * The size of the copy is known a priori - and stored in the inflated size.
    * no allocation or deallocation is performed in this routine
    *
    * @return the inflated size
    */
  def inflateTo(destination: TeslaMemoryPtr): Long

  /**
    * decompress a source deflated parcel into a provided parcel.  This parcel must have enough
    * memory to accommodate the inflated size.  This call will throw an error if you inflate
    * from a non-deflated parcel or if you try and inflate to the source parcel
    *
    */
  def inflateFrom(source: TeslaParcel): Unit

  /**
    * compress this parcel into a given native memory location.
    * The size of the copy is not known a priori - it will be less than the inflated size.
    * no allocation or deallocation is performed in this routine
    *
    * @return
    */
  def deflateTo(destination: TeslaMemoryPtr): Long

  /**
    * uncompress a source parcel into this parcel. This parcel must have enough
    * * memory to accommodate the deflated size
    * The size of the copy is not known a priori - it will be less than the inflated size.
    * no allocation or deallocation is performed in this routine
    *
    * @return
    */
  def deflateFrom(destination: TeslaParcel): Unit

  //////////////////////////////////////////////////////////////////////////////////////////
  // reading
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
    * start reads
    */
  def startReads(): Unit

  /**
    * get the next buffer in this parcel. The parcel must be
    * inflated. Each buffer returned is alloc'ed within this routine
    * and must be freed external to this routine.
    *
    * @return
    */
  def readNextBuffer: TeslaMutableBuffer

  //////////////////////////////////////////////////////////////////////////////////////////
  // writing
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
    * start writes
    */
  def startWrites(): Unit

  /**
    * add a buffer to this parcel. The parcel must be
    * inflated.
    *
    * @param buffer the buffer to append
    * @return the available bytes remaining or -1 if too big
    */
  def writeNextBuffer(buffer: TeslaBuffer): TeslaMemorySize

  //////////////////////////////////////////////////////////////////////////////////////////
  // ingress/egress
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Copy the contents of a parcel into this parcel
   * @param parcel the parcel to copy
   */
  def copyFrom(parcel: TeslaParcel): Unit

  /**
    * output bytes from this parcel into a heap byte array
    *
    * @return
    */
  def toHeapArray: Array[Byte]

  /**
    * load bytes into this parcel from a heap byte array
    *
    */
  def fromHeapArray(bytes: Array[Byte]): Unit

  /**
    * load a deflated bytes from a memory pointer
    */
  def fromDeflatedMemoryPtr(number: Int, inflatedSize: TeslaMemorySize, deflatedSizr: TeslaMemorySize, source: TeslaMemoryPtr): Unit

  /**
    * get the data in this wrapped in a direct byte buffer
    */
  def asByteBuffer: ByteBuffer

}

final case
class TeslaParcelAnyVal(blockPtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with TeslaParcelState with TeslaParcelReader with TeslaParcelWriter with TeslaParcelInflator with TeslaParcelDeflator
  with TeslaParcelBulk {

  override def currentMemorySize: TeslaMemoryOffset = ??? // TODO

  //////////////////////////////////////////////////////////////////////////////////////////
  // misc
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline
  override def toString: String = {
    if (blockPtr > 0) {
      val deflatedSizeString = if (isInflated) "(INFLATED)" else deflatedSize
      s"TeslaParcel(blockPtr=$blockPtr, parcelStartPtr=$parcelStartPtr, isInflated=$isInflated, inflatedSize=$inflatedSize, deflatedSize=$deflatedSizeString, bufferCount=$bufferCount)"
    } else {
      status.statusName
    }
  }

}
