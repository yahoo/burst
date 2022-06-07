/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.runtime

import org.burstsys.vitals.text.VitalsTextCodec
import org.joda.time.{DateTimeZone, MutableDateTime}

/**
 * There is only one thread runtime per thread at any point in the execution. This means
 * that in any given operation, there may be multiple of these in scope for a load/scan etc.
 * Nothing here has to be multi-thread protected.
 */
trait BrioThreadRuntime extends AnyRef {

  /////////////////////////////////////
  // State
  /////////////////////////////////////

  private[this]
  val _text: VitalsTextCodec = VitalsTextCodec()

  private[this]
  var _invalidQuery: Boolean = false

  private[this]
  var _hadException: Boolean = false

  private[this]
  var _hadNull: Boolean = false

  private[this]
  val _time: MutableDateTime = new MutableDateTime()

  /////////////////////////////////////
  // errors
  /////////////////////////////////////

  final def error(t: Throwable): Unit = throw t

  /////////////////////////////////////
  // Lifecycle
  /////////////////////////////////////

  @inline final
  def prepareBrioThreadRuntime(timezone: DateTimeZone): Unit = {
    _invalidQuery = false
    _hadException = false
    _hadNull = false
    _time setZone timezone
  }

  /////////////////////////////////////
  // Accessors
  /////////////////////////////////////

  @inline final implicit
  def text: VitalsTextCodec = _text

  @inline final implicit
  def time: MutableDateTime = _time

  @inline final
  def hadNull: Boolean = _hadNull

  @inline final
  def hadNull(s: Boolean): Unit = _hadNull = s

  @inline final
  def invalidQuery: Boolean = _invalidQuery

  @inline final
  def invalidQuery(s: Boolean): Unit = _invalidQuery = s

  @inline final
  def hadException: Boolean = _hadException

  @inline final
  def hadException(s: Boolean): Unit = _hadException = s

  @inline final
  def aborted: Boolean = invalidQuery || hadNull || hadException

  @inline final
  def defaultBoolean: Boolean = {
    false
  }

  @inline final
  def defaultByte: Byte = {
    0.toByte
  }

  @inline final
  def defaultShort: Short = {
    0.toShort
  }

  @inline final
  def defaultInt: Int = {
    0.toInt
  }

  @inline final
  def defaultLong: Long = {
    0.toLong
  }

  @inline final
  def defaultDouble: Double = {
    0.toDouble
  }

  @inline final
  def defaultString: String = {
    ""
  }

}
