/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.container

import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.dictionary.key.BrioDictionaryKeyAnyVal
import org.burstsys.brio.dictionary.slot.BrioDictionarySlotValue
import org.burstsys.brio.types.BrioTypes.{BrioDictionaryKey, BrioDictionaryNotFound}
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.text.VitalsTextCodec

/**
 * Engine for reading dictionaries
 */
trait BrioDictionaryReadAccessor extends Any with BrioDictionary with BrioDictionaryInternals {

  //////////////////////////////////////////////////////////////////////////////////////////
  // Then we need to do something with those keys and strings
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def keyLookup(string: String)(implicit text: VitalsTextCodec): BrioDictionaryKey = {

    if (overflowed) {
      log warn s"keyLookup overflowed"
      return BrioDictionaryNotFound
    }

    if (string == null) {
      log warn s"keyLookup NULL_STRING_PASS"
      return BrioDictionaryNotFound
    }

    val bytes = text.encode(string)

    // get the stored offset from the appropriate bucket
    var slotOffset = bucketOffsetValue(bucketIndex(string))

    // now try to scan down it, if its empty then return not found
    while (slotOffset != NullSlotOffset) {
      // we have more slots in our bucket list - get the current slot
      val slot = BrioDictionarySlotValue(basePtr + slotOffset)

      // check for a string match
      if (slot.stringMatches(bytes)) return slot.key

      // not found - try the next one...
      slotOffset = slot.link
    }

    // no joy after scan - return not found
    BrioDictionaryNotFound
  }

  @inline final override
  def stringLookup(key: BrioDictionaryKey)(implicit text: VitalsTextCodec): String = {

    if (overflowed) {
      log warn s"stringLookup OVERFLOWED for key=$key"
      return null
    }

    /*
    if (key < 0) {
      log warn s"stringLookup NEGATIVE_KEY_PASS for key=$key"
      return null
    }
    */

    // turn the brio key to a dictionary key
    val zk = BrioDictionaryKeyAnyVal(key)

    // from that we can get the bucket
    val bucket = zk.bucket

    // given the bucket, we can start to scan the bucket list
    var slotLink = bucketOffsetValue(bucket)

    // continue the scan until we see an empty (null) link
    while (slotLink != NullSlotOffset) {

      // get our slot wrapper
      val slot = BrioDictionarySlotValue(basePtr + slotLink)

      // check for a key match
      if (slot.key == key) return slot.asString

      // move to the next slot in the bucket list
      slotLink = slot.link
    }

    log warn s"stringLookup NO SLOT FOUND for key=$key"
    null
  }

  @inline override
  def keySet(implicit text: VitalsTextCodec): Array[BrioDictionaryKey] = {

    if (overflowed)
      return Array.empty

    val keySet = new Array[BrioDictionaryKey](words)
    var keyIndex = 0

    // go through buckets
    var i = 0
    while (i < BucketCount) {
      tesla.offheap.getInt(basePtr + bucketsStart + (i * SizeOfInteger)) match {
        case NullSlotOffset =>
        case bucketStartOffset =>
          var bucketPtr = bucketStartOffset
          while (bucketPtr != NullSlotOffset) {
            val slot = BrioDictionarySlotValue(basePtr + bucketPtr)
            keySet(keyIndex) = slot.key
            keyIndex += 1
            bucketPtr = slot.link
          }
      }
      i += 1
    }
    keySet
  }

  @inline override
  def dump(implicit text: VitalsTextCodec): String = {
    keySet.map(k => s"$k -> '${stringLookup(k)}'").mkString(s"(\n\t", s",\n\t", s"\n)")
  }

}
