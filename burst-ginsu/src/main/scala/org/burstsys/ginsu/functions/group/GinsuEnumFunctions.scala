/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.functions.group

import org.burstsys.brio.runtime.BrioThreadRuntime

/**
 * a ginsu function that compares an input value to a set of possible provided enum values and returns either the one
 * that matches or the 'other' (last) category
 * TODO: This signature requires the creation of a free floating [[Array]] - we can certainly generate code for this without that.
 *
 */
trait GinsuEnumFunctions extends Any {

  @inline final def booleanEnumSlice(values: Array[Boolean], v: Boolean)(implicit threadRuntime: BrioThreadRuntime): Boolean = {
    var i = 0
    while (i < values.length) {
      if (values(i) == v)
        return v
      i += 1
    }
    values(values.length - 1)
  }

  @inline final def byteEnumSlice(values: Array[Byte], v: Byte)(implicit threadRuntime: BrioThreadRuntime): Byte = {
    var i = 0
    while (i < values.length) {
      if (values(i) == v)
        return v
      i += 1
    }
    values(values.length - 1)
  }

  @inline final def shortEnumSlice(values: Array[Short], v: Short)(implicit threadRuntime: BrioThreadRuntime): Short = {
    var i = 0
    while (i < values.length) {
      if (values(i) == v)
        return v
      i += 1
    }
    values(values.length - 1)
  }

  @inline final def intEnumSlice(values: Array[Int], v: Int)(implicit threadRuntime: BrioThreadRuntime): Int = {
    var i = 0
    while (i < values.length) {
      if (values(i) == v)
        return v
      i += 1
    }
    values(values.length - 1)
  }

  @inline final def longEnumSlice(values: Array[Long], v: Long)(implicit threadRuntime: BrioThreadRuntime): Long = {
    var i = 0
    while (i < values.length) {
      if (values(i) == v)
        return v
      i += 1
    }
    values(values.length - 1)
  }

  @inline final def doubleEnumSlice(values: Array[Double], v: Double)(implicit threadRuntime: BrioThreadRuntime): Double = {
    var i = 0
    while (i < values.length) {
      if (values(i) == v)
        return v
      i += 1
    }
    values(values.length - 1)
  }

  @inline final def stringEnumSlice(values: Array[String], v: String)(implicit threadRuntime: BrioThreadRuntime): String = {
    var i = 0
    while (i < values.length) {
      if (values(i).equals(v))
        return v
      i += 1
    }
    values(values.length - 1)
  }

  // TODO EXTENDED TYPES

}
