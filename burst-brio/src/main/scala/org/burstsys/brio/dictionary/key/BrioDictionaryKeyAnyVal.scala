/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.key

import org.burstsys.brio.dictionary.container._
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.vitals.errors.VitalsException

/**
  * This is a wrapper value class to support the manipulation of BrioDictionaryKey instances
  * in the context of a Zap Dictionary.
  *
  * The bucket is the bottom bits of the key
  * The ordinal is the top bits of the key
  *
  * @param data
  */
final case
class BrioDictionaryKeyAnyVal(data: BrioDictionaryKey = 0.toShort) extends AnyVal {

  /**
    * set the bucket component of the key. Note you have to use the result of this method
    * because its a value class and is immutable
    *
    * @param b
    * @return
    */
  @inline
  def bucket(b: Int): BrioDictionaryKeyAnyVal = {
    if (b > BucketCount - 1 || b < 0)
      throw VitalsException(s"bucket size larger than $BucketCount or less than zero")
    // merge the bucket into the lower bits
    val newData = (data & OrdinalMask) | b
    BrioDictionaryKeyAnyVal(newData.toShort)
  }

  /**
    * read the bucket component of the key
    *
    * @return
    */
  @inline
  def bucket: Int = data & BucketMask

  /**
    * set the ordinal component of the key. Note you have to use the result of this method
    * because its a value class and is immutable
    *
    * @param value
    * @return
    */
  @inline
  def ordinal(value: Int): BrioDictionaryKeyAnyVal = {
    if (value > MaxOrdinal - 1 || value < 0)
      throw VitalsException(s"ordinal size larger than $MaxOrdinal or less than zero")
    // merge the ordinal into the upper bits
    val newData = value << BucketBits | bucket
    BrioDictionaryKeyAnyVal(newData.toShort)
  }

  /**
    * get the ordinal component of the key
    *
    * @return
    */
  @inline
  def ordinal: Int = data >>> BucketBits

}
