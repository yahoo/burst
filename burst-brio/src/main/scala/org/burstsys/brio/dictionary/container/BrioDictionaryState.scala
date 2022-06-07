/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.container

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary.flex.BrioDictionaryBuilder
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.{SizeOfInteger, TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.{VitalsException, _}

/**
 * manage off heap state associated with a dictionary
 * '''NOTE: '''The state trait must be a universal trait separate from the AnyVal object - don't ask me why...
 */
trait BrioDictionaryState extends Any with BrioDictionaryInternals {

  //////////////////////////////////////////////////////////////////////////////////////////
  // memory needed/used for this dictionary
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def currentMemorySize: TeslaMemoryOffset = nextSlotOffset

  //////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * call when first created, only once
   *
   * @return
   */
  @inline final
  def initialize(id: TeslaPoolId, builder: BrioDictionaryBuilder): Unit = {
    poolId(id)
    words = 0
    nextSlotOffset(0)
    initializeBuckets()
  }

  /**
   * call each time you want to reuse the dictionary
   *
   * @return
   */
  @inline final
  def reset(builder: BrioDictionaryBuilder): Unit = {
    words = 0
    nextSlotOffset(0)
    initializeBuckets()
  }

  @inline private
  def initializeBuckets(): Unit = {
    val base = basePtr + bucketsStart
    if (false) { // for some reason there is suspicion that this is slower...
      tesla.offheap.setMemory(base, BucketCount * SizeOfInteger, 0)
    } else {
      // TODO profile this more
      var i = 0
      while (i < BucketCount) {
        val ptr = base + (i * SizeOfInteger)
        tesla.offheap.putInt(ptr, NullSlotOffset)
        i += 1
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // memory needed/used for this dictionary during SERDE
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def serializationSize: TeslaMemorySize = {
    // Keeping a buffer of 1024 as downstream code checks for room before writing
    if (nextSlotOffset == NullSlotOffset) firstSlotOffset + 1024
    else nextSlotOffset + 1024
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // first integer in the memory block is the count of words
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def words: Int = {
    val p = checkPtr(basePtr)
    tesla.offheap.getInt(p)
  }

  @inline final override
  def words_=(w: Int): Unit = {
    val p = checkPtr(basePtr)
    tesla.offheap.putInt(p, w)
  }

  @inline final
  def overflowed: Boolean = words < 0

  @inline final
  def keyOverflowed: Boolean = words == keyOverflow

  @inline final
  def slotOverflowed: Boolean = words == slotOverflow

  @inline final override
  def flagOverflow(): Unit = words = unknownOverflow

  @inline final override
  def flagKeyOverflow(): Unit = words = keyOverflow

  @inline final override
  def flagSlotOverflow(): Unit = words = slotOverflow

  //////////////////////////////////////////////////////////////////////////////////////////
  // next integer in the memory block is the pool id
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def poolIdStart: TeslaMemoryOffset = SizeOfInteger

  @inline final override
  def poolId: TeslaPoolId = {
    val p = checkPtr(basePtr + poolIdStart)
    tesla.offheap.getInt(p)
  }

  @inline final override
  def poolId(w: TeslaPoolId): Unit = {
    val p = checkPtr(basePtr + poolIdStart)
    tesla.offheap.putInt(p, w)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // next integer in the memory block is the offset to the next slot (starts at zero)
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def nextSlotOffsetStart: TeslaMemoryOffset = poolIdStart + SizeOfInteger

  @inline final override
  def nextSlotOffset: TeslaMemoryOffset = {
    val p = checkPtr(basePtr + nextSlotOffsetStart)
    tesla.offheap.getInt(p)
  }

  @inline final override
  def nextSlotOffset(offset: Int): Unit = {
    val p = checkPtr(basePtr + nextSlotOffsetStart)
    tesla.offheap.putInt(p, offset)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // next comes an array of buckets in the memory block
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def bucketsStart: TeslaMemoryOffset = nextSlotOffsetStart + SizeOfInteger

  @inline final override
  def bucketIndex(s: String): Int = math.abs(s.hashCode) % BucketCount

  @inline final override
  def bucketOffsetValue(index: Int): TeslaMemoryOffset = {
    val p = checkPtr(basePtr + bucketsStart + (SizeOfBucket * index))
    tesla.offheap.getInt(p)
  }

  @inline final override
  def bucketOffsetValue(index: Int, offset: TeslaMemoryOffset): Unit = {
    val p = checkPtr(basePtr + bucketsStart + (SizeOfBucket * index))
    tesla.offheap.putInt(p, offset)
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // then comes an sequence of string storage slots in the memory block
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def firstSlotOffset: TeslaMemoryOffset = bucketsStart + (SizeOfBucket * BucketCount)

  //////////////////////////////////////////////////////////////////////////////////////////
  // loading
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def importDictionary(sourceDictionary: TeslaBlockPart, sourceItems: Int): Unit = {
    val localPoolId = this.poolId
    tesla.offheap.copyMemory(sourceDictionary.basePtr, basePtr, sourceDictionary.currentMemorySize)
    this.poolId(localPoolId)
    this.words = sourceItems
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Kryo encode/decode
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def write(k: Kryo, out: Output): Unit = {
    try {
      // header ---------------------------------------
      out writeInt words
      out writeInt poolId
      out writeInt nextSlotOffset

      // buckets ---------------------------------------
      for (index <- 0 until BucketCount) out.writeInt(bucketOffsetValue(index))

      // slots ---------------------------------------
      var cursor = basePtr + firstSlotOffset
      while (cursor < basePtr + nextSlotOffset) {
        val p = checkPtr(cursor)
        out.writeByte(tesla.offheap.getByte(p))
        cursor += 1
      }
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)

    }
  }

  @inline final
  def read(k: Kryo, in: Input): Unit = {
    try {
      val localPoolId = this.poolId // keep local pool id

      // header ---------------------------------------
      words = in.readInt // -1 for overflow...
      in.readInt // throw away remote pool id
      nextSlotOffset(in.readInt)

      // buckets ---------------------------------------
      for (index <- 0 until BucketCount) bucketOffsetValue(index, in.readInt)

      // slots ---------------------------------------
      var cursor = basePtr + firstSlotOffset
      while (cursor < basePtr + nextSlotOffset) {
        val p = checkPtr(cursor)
        tesla.offheap.putByte(p, in.readByte)
        cursor += 1
      }

      // re-assert local pool id for yucks
      poolId(localPoolId)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
