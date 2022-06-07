/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.container

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.pool.TeslaPoolId

/**
  *
  **/
trait BrioDictionaryInternals extends Any with TeslaBlockPart {

  ////////////////////////////////////////////////////////////////////////////////////////

  /**
    * set the count of words in dictionary (-1 for overflow)
    *
    * @param w
    */
  def words_=(w: Int): Unit

  /**
    * flag this dictionary as in an general overflow condition
    */
  def flagOverflow(): Unit

  /**
    * flag this dictionary as in a key exhaustion overflow condition
    */
  def flagKeyOverflow(): Unit

  /**
    * flag this dictionary as in a slot exhaustion overflow condition
    */
  def flagSlotOverflow(): Unit

  ////////////////////////////////////////////////////////////////////////////////////////

  /**
    * set the pool id for this dictionary
    *
    * @param w
    */
  def poolId(w: TeslaPoolId): Unit

  /**
    * the pool id for this dictionary
    *
    * @return
    */
  def poolId: TeslaPoolId

  ////////////////////////////////////////////////////////////////////////////////////////

  /**
    * the start offset of slot storage
    *
    * @return
    */
  def firstSlotOffset: TeslaMemoryOffset

  /**
    * the offset into the dictionary of the next available slot (the current end of the dictionary)
    *
    * @return
    */
  def nextSlotOffset: Int

  /**
    * set the offset into the dictionary of the next available slot (the current end of the dictionary)
    *
    * @param offset
    */
  def nextSlotOffset(offset: Int): Unit

  ////////////////////////////////////////////////////////////////////////////////////////

  /**
    * the offset of the beginning of the bucket array
    *
    * @return
    */
  def bucketsStart: TeslaMemoryOffset

  /**
    * get the value for a given bucket index
    *
    * @param index
    * @return
    */
  def bucketOffsetValue(index: Int): TeslaMemoryOffset

  /**
    * set the value for a given bucket index
    *
    * @param index
    * @param offset
    */
  def bucketOffsetValue(index: Int, offset: TeslaMemoryOffset): Unit

  /**
    * calculate the bucket index for a given string
    *
    * @param s
    * @return
    */
  def bucketIndex(s: String): Int

  ////////////////////////////////////////////////////////////////////////////////////////

  /**
    * total serialization size (used for memory allocation during serialization)
    *
    * @return
    */
  def serializationSize: TeslaMemorySize

}
