/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.functions.group

import org.burstsys.brio.runtime.BrioThreadRuntime

/**
 * a ginsu function that compares an input value to a set of possible provided split boundary ranges and returns either the one
 * that matches or the a max negative or max positive value for below or above
 * TODO: This signature requires the creation of a free floating [[Array]] - we can certainly generate code for this without that.
 */
trait GinsuSplitFunctions extends Any {

  @inline final def booleanSplitSlice(s: Array[Boolean], v: Boolean)(implicit threadRuntime: BrioThreadRuntime): Boolean =
    ???

  @inline final def byteSplitSlice(s: Array[Byte], v: Byte)(implicit threadRuntime: BrioThreadRuntime): Byte = {
    if (v < s(0)) return Byte.MinValue
    if (v >= s(s.length - 1)) return Byte.MaxValue
    var i = 0
    var last: Byte = Byte.MinValue
    while (i < s.length) {
      val b = s(i)
      if (v >= last && v < b)
        return last
      last = b
      i += 1
    }
    throw new RuntimeException("should never happen")
  }

  @inline final def shortSplitSlice(s: Array[Short], v: Short)(implicit threadRuntime: BrioThreadRuntime): Short = {
    if (v < s(0)) return Short.MinValue
    if (v >= s(s.length - 1)) return Short.MaxValue
    var last: Short = Short.MinValue
    var i = 0
    while (i < s.length) {
      val b = s(i)
      if (v >= last && v < b)
        return last
      last = b
      i += 1
    }
    throw new RuntimeException("should never happen")
  }

  @inline final def intSplitSlice(s: Array[Int], v: Int)(implicit threadRuntime: BrioThreadRuntime): Int = {
    if (v < s(0)) return Int.MinValue
    if (v >= s(s.length - 1)) return Int.MaxValue
    var last: Int = Int.MinValue
    var i = 0
    while (i < s.length) {
      val b = s(i)
      if (v >= last && v < b)
        return last
      last = b
      i += 1
    }
    throw new RuntimeException("should never happen")
  }

  @inline final def longSplitSlice(s: Array[Long], v: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    if (v < s(0)) return Long.MinValue
    if (v >= s(s.length - 1)) return Long.MaxValue
    var last: Long = Long.MinValue
    var i = 0
    while (i < s.length) {
      val b = s(i)
      if (v >= last && v < b)
        return last
      last = b
      i += 1
    }
    throw new RuntimeException("should never happen")
  }

  @inline final def doubleSplitSlice(s: Array[Double], v: Double)(implicit threadRuntime: BrioThreadRuntime): Double = {
    // if the value is less than the first number
    if (v < s(0)) return Double.NaN
    // if the value is greater than or equal to the last value
    if (v >= s(s.length - 1)) return Double.NaN
    var last: Double = Double.NaN
    //    var last: Double = Double.NaN
    var i = 0
    while (i < s.length) {
      val b = s(i)
      if (v >= last && v < b)
        return last
      last = b
      i += 1
    }
    throw new RuntimeException("should never happen")
  }

  // TODO EXTENDED TYPES

  @inline final def stringSplitSlice(s: Array[String], v: String)(implicit threadRuntime: BrioThreadRuntime): String =
    ???


}
