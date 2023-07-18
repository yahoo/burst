/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTimeZone
import org.joda.time.MutableDateTime

import java.text.DecimalFormat

/**
 * A set of formatting functions for use in instrumentation reporting
 */
package object instrument extends VitalsLogger {

  final val KB: Long = math.pow(2, 10).toLong
  final val MB: Long = math.pow(2, 20).toLong
  final val GB: Long = math.pow(2, 30).toLong
  final val TB: Long = math.pow(2, 40).toLong
  final val PB: Long = math.pow(2, 50).toLong

  def prettyFixedNumber(number: Long): String = {
    new DecimalFormat().format(number)
  }

  def prettyFixedNumber(number: Int): String = {
    new DecimalFormat().format(number)
  }

  def prettyFloatNumber(number: Double): String = {
    f"$number%2.2f"
  }

  def prettyBandwidthString(dataSize: Long, elapsedNanos: Long): String = {
    val bytesPerSec: Double = if (elapsedNanos.toDouble == 0) 0 else (dataSize / elapsedNanos.toDouble) * 1E9
    s"${prettyByteSizeString(dataSize)} in ${prettyTimeFromNanos(elapsedNanos)} (${prettyByteSizeString(bytesPerSec.toLong)}/sec)"
  }

  def prettyRateString(thing: String, items: Long, elapsedNanos: Long): String = {
    val rate: Double = if (elapsedNanos.toDouble == 0) 0 else (items / elapsedNanos.toDouble) * 1E9
    val suffix = s"$thing(s) per sec"
    if (rate == 0) {
      f"0 $suffix"
    } else if (rate < 1E3) {
      f"${rate / 1}%,.1f $suffix"
    } else if (rate < 1E6) {
      f"${rate / 1E3}%,.1fK $suffix"
    } else if (rate < GB) {
      f"${rate / 1E6}%,.1fM $suffix"
    } else if (rate < TB) {
      f"${rate / 1E9}%,.1fG $suffix"
    } else {
      f"${rate / 1E12}%,.1fT $suffix"
    }
  }

  def prettyPeriodString(thing: String, items: Long, elapsedNanos: Long): String = {
    val period: Double = if (items == 0) 0 else elapsedNanos.toDouble / 1E9 / items
    val suffix = s"sec per $thing"
    if (period == 0) {
      f"0 $suffix"
    } else if (period > 1) {
      f"$period%,.1f $suffix"
    } else if (period > 1 / 1E3) {
      f"${period * 1E3}%,.1f m$suffix"
    } else if (period > 1 / 1E6) {
      f"${period * 1E6}%,.1f u$suffix"
    } else if (period > 1 / 1E9) {
      f"${period * 1E9}%,.1f n$suffix"
    } else {
      f"${period * 1E12}%,.1f p$suffix"
    }
  }

  /**
   * Print a byte size rounded to the nearest B/KB/MB/GB/TB
   * @param bytes the number of bytes
   * @return a formatted string like 1.2KB or 3.9GB
   */
  def prettyByteSizeString(bytes: Double): String = {
    if (bytes < 0) {
      s"not supported/unknown bytes=$bytes"
    } else if (bytes < KB) {
      f"$bytes%,.1fB"
    } else if (bytes < MB) {
      f"${bytes / KB}%,.1fKB"
    } else if (bytes < GB) {
      f"${bytes / MB}%,.1fMB"
    } else if (bytes < TB) {
      f"${bytes / GB}%,.1fGB"
    } else {
      f"${bytes / TB}%,.1fTB"
    }
  }

  /**
   * Print a byte size rounded to the nearest B/KB/MB/GB/TB
   * @param bytes the number of bytes
   * @return a formatted string like 1.2KB or 3.9GB
   */
  def prettyByteSizeString(bytes: Long): String = prettyByteSizeString(bytes.toDouble)

  def prettySizeString(size: Double): String = {
    if (size < 1E3) {
      f"$size%,.1f"
    } else if (size < 1E6) {
      f"${size / 1E3}%,.1fK"
    } else if (size < 1E9) {
      f"${size / 1E6}%,.1fM"
    } else if (size < 1E12) {
      f"${size / 1E9}%,.1fG"
    } else {
      f"${size / 1E12}%,.1fT" // you wish...
    }
  }

  def prettySizeString(size: Long): String = prettySizeString(size.toDouble)

  def prettyDateTimeFromMillis(ms: Long): VitalsUid = {
    new MutableDateTime(ms, DateTimeZone.UTC).toString(unspacedFormat)
  }

  private
  val unspacedFormat = DateTimeFormat.forPattern("YYYY.MM.dd-KK.mm.ss-aa-zzz")

  def prettyDateTimeFromMillisNoSpaces(ms: Long): VitalsUid = {
    new MutableDateTime(ms, DateTimeZone.UTC).toString(unspacedFormat)
  }

  def prettyPercentage(value: Double): String = {
    f"$value%2.2f"
  }

  def prettyPercentage(top: Long, bottom: Long): String = {
    if (bottom == 0) {
      "NaN"
    } else {
      f"${(top.toDouble / bottom.toDouble) * 100}%2.2f"
    }
  }

  def prettyPercentage(top: Int, bottom: Int): String = prettyPercentage(top.toLong, bottom.toLong)

  def prettyTimeFromNanos(nanos: Long): String =
    prettyTimeFromNanos(nanos.toDouble)

  def prettyTimeFromNanos(nanos: Double): String = {
    if (nanos > 1e9 * 60 * 60 * 24) {
      f"${nanos / (1E9 * 60.0 * 60 * 24)}%.1fd"
    } else if (nanos > 1E9 * 60 * 60) {
      f"${nanos / (1E9 * 60.0 * 60)}%.1fh"
    } else if (nanos > 1E9 * 60) {
      f"${nanos / (1E9 * 60.0)}%.1fm"
    } else if (nanos > 1E9) {
      f"${nanos / 1E9}%.1fs"
    } else if (nanos > 1e6) {
      f"${nanos / 1e6}%.1fms"
    } else if (nanos > 1e3) {
      f"${nanos / 1e3}%.1fus"
    } else {
      f"$nanos%.1fns"
    }
  }

  def prettyTimeFromMillis(milliseconds: Long): String = {
    prettyTimeFromMillis(milliseconds.toDouble)
  }

  def prettyTimeFromMillis(milliseconds: Double): String = {
    prettyTimeFromNanos(milliseconds * 1E6)
  }

  def displayCountAverage(count: Int, unit: String, nanoseconds: Long): String = {
    val average = if (count == 0) "NA" else prettyTimeFromNanos(nanoseconds / count)
    f"${prettySizeString(count)} $unit(s) in ${prettyTimeFromNanos(nanoseconds)} ($average/$unit)"
  }

}
