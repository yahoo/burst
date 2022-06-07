/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.runtime.group

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.group.GinsuSplitFunctions


/**
 * the runtime extension for [[GinsuSplitFunctions]]
 */
trait GinsuSplitRuntime extends GinsuSplitFunctions {

  implicit def threadRuntime: BrioThreadRuntime

  @inline final def booleanSplitSlice(s: Array[Boolean], v: Boolean): Boolean =
    super.booleanSplitSlice(s, v)(threadRuntime)

  @inline final def byteSplitSlice(s: Array[Byte], v: Byte): Byte =
    super.byteSplitSlice(s, v)(threadRuntime)

  @inline final def shortSplitSlice(s: Array[Short], v: Short): Short =
    super.shortSplitSlice(s, v)(threadRuntime)

  @inline final def intSplitSlice(s: Array[Int], v: Int): Int =
    super.intSplitSlice(s, v)(threadRuntime)

  @inline final def longSplitSlice(s: Array[Long], v: Long): Long =
    super.longSplitSlice(s, v)(threadRuntime)

  @inline final def doubleSplitSlice(s: Array[Double], v: Double): Double =
    super.doubleSplitSlice(s, v)(threadRuntime)

  @inline final def stringSplitSlice(s: Array[String], v: String): String =
    super.stringSplitSlice(s, v)(threadRuntime)

}
