/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.runtime.coerce

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.coerce.GinsuCoerceFunctions

/**
 * the runtime extension for [[GinsuCoerceFunctions]]
 */
trait GinsuCoerceRuntime extends GinsuCoerceFunctions {

  implicit def threadRuntime: BrioThreadRuntime

  @inline final def stringBooleanCoerce(v: String): Boolean =
    super.stringBooleanCoerce(v)(threadRuntime)

  @inline final def stringByteCoerce(v: String): Byte =
    super.stringByteCoerce(v)(threadRuntime)

  @inline final def stringShortCoerce(v: String): Short =
    super.stringShortCoerce(v)(threadRuntime)

  @inline final def stringIntCoerce(v: String): Int =
    super.stringIntCoerce(v)(threadRuntime)

  @inline final def stringLongCoerce(v: String): Long =
    super.stringLongCoerce(v)(threadRuntime)

  @inline final def stringDoubleCoerce(v: String): Double =
    super.stringDoubleCoerce(v)(threadRuntime)

  @inline final def intDoubleCoerce(v: Int): Double =
    super.intDoubleCoerce(v)(threadRuntime)

  @inline final def longDoubleCoerce(v: Long): Double =
    super.longDoubleCoerce(v)(threadRuntime)

  @inline final def byteDoubleCoerce(v: Byte): Double =
    super.byteDoubleCoerce(v)(threadRuntime)

  @inline final def shortDoubleCoerce(v: Short): Double =
    super.shortDoubleCoerce(v)(threadRuntime)

}
