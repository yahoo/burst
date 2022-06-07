/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.types

import org.burstsys.brio.types.BrioCourse._
import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.types.BrioTypes.{Elastic, Lookup}
import org.burstsys.vitals.text.VitalsTextCodec

/**
  * Brio supports a set of `datatypes` with associated `primitive` forms. This is a 64 bit storage format for
  * all of the datatypes with transforms to and from the language normal form. Bottom line is that
  * for all calculations, we convert to these primitive forms to keep storage such as registers simple
  * to allocate and manage.
  */
object BrioPrimitives {

  // TODO convert this to a Value class
  type BrioPrimitive = Long

  val minBrioNumber: BrioPrimitive = Long.MinValue

  val maxBrioNumber: BrioPrimitive = Long.MaxValue

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BOOLEAN
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def brioBooleanToPrimitive(v: Boolean): BrioPrimitive = if (v) 1L else 0L

  @inline final
  def brioPrimitiveToBoolean(v: BrioPrimitive): Boolean = v == 1L

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BOOLEAN
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def brioByteToPrimitive(v: Byte): BrioPrimitive = v.toLong

  @inline final
  def brioPrimitiveToByte(v: BrioPrimitive): Byte = v.toByte

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BOOLEAN
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def brioShortToPrimitive(v: Short): BrioPrimitive = v.toLong

  @inline final
  def brioPrimitiveToShort(v: BrioPrimitive): Short = v.toShort

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BOOLEAN
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def brioIntegerToPrimitive(v: Int): BrioPrimitive = v.toLong

  @inline final
  def brioPrimitiveToInteger(v: BrioPrimitive): Int = v.toInt

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BOOLEAN
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def brioLongToPrimitive(v: Long): BrioPrimitive = v.toLong

  @inline final
  def brioPrimitiveToLong(v: BrioPrimitive): Long = v.toLong

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // DOUBLE
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def brioDoubleToPrimitive(v: Double): BrioPrimitive =
    java.lang.Double.doubleToLongBits(v)

  @inline final
  def brioPrimitiveToDouble(v: BrioPrimitive): Double = java.lang.Double.longBitsToDouble(v)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STRING
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def brioStringToPrimitive(d: BrioDictionary, v: String)(implicit text: VitalsTextCodec): BrioPrimitive =
    d.keyLookupWithAdd(v).toLong

  @inline final
  def brioPrimitiveToString(d: BrioDictionary, v: BrioPrimitive)(implicit text: VitalsTextCodec): String =
    d.stringLookup(v.toShort)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // COURSES
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def brioCourse32Primitive(v: BrioCourse32): BrioPrimitive = v.data

  @inline final
  def brioPrimitiveToCourse32(v: BrioPrimitive): BrioCourse32 = BrioCourse32(v)

  @inline final
  def brioCourse16Primitive(v: BrioCourse16): BrioPrimitive = v.data

  @inline final
  def brioPrimitiveToCourse16(v: BrioPrimitive): BrioCourse16 = BrioCourse16(v)

  @inline final
  def brioCourse8Primitive(v: BrioCourse8): BrioPrimitive = v.data

  @inline final
  def brioPrimitiveToCourse8(v: BrioPrimitive): BrioCourse8 = BrioCourse8(v)

  @inline final
  def brioCourse4Primitive(v: BrioCourse4): BrioPrimitive = v.data

  @inline final
  def brioPrimitiveToCourse4(v: BrioPrimitive): BrioCourse4 = BrioCourse4(v)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ELASTIC
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  // TODO EXTENDED TYPES
  @inline final
  def brioElasticToPrimitive(v: Elastic): BrioPrimitive = v.toLong

  @inline final
  def brioPrimitiveToElastic(v: BrioPrimitive): Elastic = v.toLong

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LOOKUP
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////

  // TODO EXTENDED TYPES
  @inline final
  def brioLookupToPrimitive(v: Lookup): BrioPrimitive = v.toLong

  @inline final
  def brioPrimitiveToLookup(v: BrioPrimitive): Lookup = v.toLong
}
