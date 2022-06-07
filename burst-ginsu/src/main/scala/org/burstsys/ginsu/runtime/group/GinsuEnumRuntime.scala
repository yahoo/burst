/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.runtime.group

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.group.GinsuEnumFunctions


/**
 * the runtime extension for [[GinsuEnumFunctions]]
 */
trait GinsuEnumRuntime extends GinsuEnumFunctions {

  implicit def threadRuntime: BrioThreadRuntime

  @inline final def booleanEnumSlice(values: Array[Boolean], v: Boolean): Boolean =
    super.booleanEnumSlice(values, v)(threadRuntime)

  @inline final def byteEnumSlice(values: Array[Byte], v: Byte): Byte =
    super.byteEnumSlice(values, v)(threadRuntime)

  @inline final def shortEnumSlice(values: Array[Short], v: Short): Short =
    super.shortEnumSlice(values, v)(threadRuntime)

  @inline final def intEnumSlice(values: Array[Int], v: Int): Int =
    super.intEnumSlice(values, v)(threadRuntime)

  @inline final def longEnumSlice(values: Array[Long], v: Long): Long =
    super.longEnumSlice(values, v)(threadRuntime)

  @inline final def doubleEnumSlice(values: Array[Double], v: Double): Double =
    super.doubleEnumSlice(values, v)(threadRuntime)

  @inline final def stringEnumSlice(values: Array[String], v: String): String =
    super.stringEnumSlice(values, v)(threadRuntime)

}
