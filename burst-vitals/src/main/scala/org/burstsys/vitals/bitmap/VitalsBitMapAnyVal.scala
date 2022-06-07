/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.bitmap

import org.burstsys.vitals.errors.VitalsException

import scala.language.implicitConversions

object VitalsBitMapAnyVal {

  implicit def longToBitMap(number: Long): VitalsBitMapAnyVal = VitalsBitMapAnyVal(number)

  implicit def BitMapToLong(map: VitalsBitMapAnyVal): Long = map.data

}

/**
  * generalized bit map functions based on scala value class
  */
final case class VitalsBitMapAnyVal(data: Long = 0L) extends AnyVal {

  import VitalsBitMapAnyVal._

  @inline
  def setBit(position: Int): VitalsBitMapAnyVal = {
    if (position > 63) throw VitalsException(s" bit ordinal out of range")
    val bit: Long = 1L << position
    VitalsBitMapAnyVal(data | bit)
  }

  @inline
  def resetBit(position: Int): VitalsBitMapAnyVal = {
    if (position > 63) throw VitalsException(s" bit ordinal out of range")
    val bit = 1L << position
    VitalsBitMapAnyVal(data & ~bit)
  }

  @inline
  def testBit(position: Int): Boolean = {
    if (position > 63) throw VitalsException(s" bit ordinal out of range")
    val bit: Long = 1L << position
    (data & bit) != 0
  }

  @inline
  def +=(bits: Array[Byte]): VitalsBitMapAnyVal = {
    var map: Long = 0L
    bits foreach {
      b =>
        map = map.setBit(b)
        b
    }
    map
  }

  @inline
  def |(mask: VitalsBitMapAnyVal): VitalsBitMapAnyVal = {
    VitalsBitMapAnyVal(data | mask.data)
  }

  @inline
  def &(mask: VitalsBitMapAnyVal): VitalsBitMapAnyVal = {
    VitalsBitMapAnyVal(data & mask.data)
  }

  def asString: String = data.toBinaryString

  override
  def toString: String = {
    def sep(i: Int): String = if (i % 4 == 3) " " else ""

    val l = for (i <- (0 to 63).reverse) yield
      if (testBit(i)) sep(i) + "1" else sep(i) + "0"
    l.foldRight("")(_ + _)
  }
}
