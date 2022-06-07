/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary

import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.logging._

package object container extends VitalsLogger {

  private[dictionary] final val NullSlotOffset = 0

  final val SizeOfBucket = SizeOfInteger

  final val KeyBits = 16 // absolute size of key (SHORT)

  // number of bits reserved for bucket identity
  final val BucketBits = 6

  // number of bits reserved for bucket ordinal
  final val OrdinalBits = KeyBits - BucketBits

  // the number of buckets (64)
  final val BucketCount: Int = math.pow(2, BucketBits).toInt

  // the number of ordinal slots in a bucket ( 1024 )
  final val MaxOrdinal: Int = math.pow(2, OrdinalBits).toInt

  // with maximum entropy (perfect hashing) - this is how many words can be stored (64K)
  final val PerfectSize = BucketCount * MaxOrdinal

  // lower BucketBits
  final val BucketMask: Short = 0x3f.toShort

  // upper OrdinalBits
  final val OrdinalMask: Short = 0xFFC0.toShort

  // sanity check
  assert(BucketBits + OrdinalBits == KeyBits)

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // overflow markers
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
    * legacy flag
    */
  final val unknownOverflow = -1

  /**
    * keyspace exhausted
    */
  final val keyOverflow = -2

  /**
    * slot space exhausted
    */
  final val slotOverflow = -3

}
