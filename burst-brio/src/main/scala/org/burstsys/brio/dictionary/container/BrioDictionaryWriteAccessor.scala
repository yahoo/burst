/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.container

import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.dictionary.key.BrioDictionaryKeyAnyVal
import org.burstsys.brio.dictionary.slot.BrioDictionarySlotValue
import org.burstsys.brio.types.BrioTypes.{BrioDictionaryKey, BrioDictionaryNotFound}
import org.burstsys.tesla.TeslaTypes.TeslaMemoryOffset
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.instrument.prettyFixedNumber
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.vitals.logging._

/**
  * API implementation for reading dictionaries
  */
trait BrioDictionaryWriteAccessor extends Any with BrioDictionaryInternals with BrioDictionary {

  def availableMemorySize: TeslaMemoryOffset

  //////////////////////////////////////////////////////////////////////////////////////////
  // public routines
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def keyLookupWithAdd(string: String)(implicit text: VitalsTextCodec): BrioDictionaryKey = {
    if (string == null)
      throw VitalsException(s"null string added to key lookup")

    if (overflowed)
      return BrioDictionaryNotFound

    var ordinal = 0 // head slot in bucket list

    /**
      * get the appropriate bucket based on a hash function
      */
    val bucket = bucketIndex(string)

    // get the contents of that bucket
    val bucketListHeadOffset = bucketOffsetValue(bucket)

    /**
      * Now that we know what is in the bucket
      * handle case of empty bucket
      */
    if (bucketListHeadOffset == NullSlotOffset) {

      // allocate a new slot
      val slot = instantiateSlot
      if (overflowed)
        return BrioDictionaryNotFound

      // update the bucket to point to this as the head of the bucket list
      bucketOffsetValue(bucket, (slot.ptr - basePtr).toInt)

      // put the string into it
      slot.stringData(string)

      // update the cursor for new slots
      nextSlotOffset((slot.ptr + slot.slotSize - basePtr).toInt)

      // create the key
      var zk = BrioDictionaryKeyAnyVal()

      // initialize it
      zk = zk.bucket(bucket)
      zk = zk.ordinal(ordinal)

      // store the key
      slot.key(zk.data)

      // return the key
      return zk.data
    }

    /**
      * we don't have an empty bucket so time to look at the bucket list
      */
    ordinal = 1 // the slot directly after the head in the bucket list

    // keep track of last one seen (start with head of bucket list)
    var priorSlotOffset = bucketListHeadOffset

    var currentSlotOffset = bucketListHeadOffset

    val bytes = text.encode(string)

    /**
      * handle case of at least one in bucket list - loop through value slots until we find a match or
      * hit the end
      */
    while (currentSlotOffset != NullSlotOffset) {

      // set up our wrapper value class
      val currentSlot = BrioDictionarySlotValue(basePtr + currentSlotOffset)

      // check for a match
      if (currentSlot.stringMatches(bytes)) return currentSlot.key

      // store the prior end of the list for later linkage
      priorSlotOffset = currentSlotOffset

      // move down the bucket list
      currentSlotOffset = currentSlot.link

      ordinal += 1 // move down the list
      if (ordinal >= MaxOrdinal) {
        log warn burstStdMsg(
          s"dictionary for string '$string', ordinal $ordinal was greater than max ordinal ($MaxOrdinal) ${
            prettyFixedNumber(words)
          } words"
        )
        flagKeyOverflow()
        return BrioDictionaryNotFound
      }
    }

    /**
      * we hit the end of the bucket list without a match. Create a new slot and link
      * it in to the end of the bucket list
      */

    // get a new slot
    val newSlot = instantiateSlot
    if (overflowed)
      return BrioDictionaryNotFound

    // point previous slot at it
    val priorSlot = BrioDictionarySlotValue(basePtr + priorSlotOffset.toLong)

    priorSlot.link((newSlot.ptr - basePtr).toInt)

    // put the string into it
    newSlot.stringData(string)

    // update the cursor for new slots
    nextSlotOffset((newSlot.ptr - basePtr).toInt + newSlot.slotSize)

    // create the key
    var zk = BrioDictionaryKeyAnyVal()

    // initialize the key
    zk = zk.bucket(bucket)
    zk = zk.ordinal(ordinal)

    // store the key
    newSlot.key(zk.data)
    newSlot.link(NullSlotOffset)

    // return the key
    zk.data
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // internal routines
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
    * allocate a new string slot at the end of the dictionary. This does not update the offset
    * for the next slot since you need to know the size of the string to do that.
    *
    * @return
    */
  @inline private
  def instantiateSlot: BrioDictionarySlotValue = {
    // increment the word count
    words = words + 1

    // check to see if we have allocated any slots yet
    val slotOffset = if (nextSlotOffset == NullSlotOffset) {
      // if not, use the first one
      nextSlotOffset(firstSlotOffset)
      firstSlotOffset
    } else nextSlotOffset // use the next one

    /**
      * here we check to see if there is enough room to add another slot.
      * We also need to check when we add a string. MUCH MORE WORK NEEDED
      * TODO we really should learn to resize these on the fly - allocate new
      * block and copy old to new. Since these are append only structures it
      * works.
      */
    if (slotOffset >= availableMemorySize - 512) {
      log warn burstStdMsg(
        s"dictionary slot request exceeded memory size $slotOffset >= $currentMemorySize (words=$words)")
      flagSlotOverflow()
      return BrioDictionarySlotValue()
    }

    // return this slot value class
    BrioDictionarySlotValue(basePtr + slotOffset).initialize
  }

}
