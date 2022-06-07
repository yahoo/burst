/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.functions.coerce

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.vitals.errors.safely

trait GinsuCoerceFunctions extends Any {

  @inline final def stringBooleanCoerce(v: String)(implicit threadRuntime: BrioThreadRuntime): Boolean = {
    if (threadRuntime.aborted) return threadRuntime.defaultBoolean
    if (v == null) {
      threadRuntime.hadNull(true)
      threadRuntime.defaultBoolean
    } else {
      val s = v.trim
      try {
        if (s.isEmpty) {
          threadRuntime.defaultBoolean
        } else {
          s.toBoolean
        }
      } catch safely {
        case e: NumberFormatException => threadRuntime.defaultBoolean
      }
    }
  }

  @inline final def stringByteCoerce(v: String)(implicit threadRuntime: BrioThreadRuntime): Byte = {
    if (threadRuntime.aborted) return threadRuntime.defaultByte
    if (v == null) {
      threadRuntime.hadNull(true)
      threadRuntime.defaultByte
    } else {
      val s = v.trim
      try {
        if (s.isEmpty) {
          threadRuntime.defaultByte
        } else {
          var i = 0
          while (i < s.length) {
            if (!s.charAt(i).isDigit) return threadRuntime.defaultByte
            i += 1
          }
          s.toByte
        }
      } catch safely {
        case e: NumberFormatException => threadRuntime.defaultByte
      }
    }
  }

  @inline final def stringShortCoerce(v: String)(implicit threadRuntime: BrioThreadRuntime): Short = {
    if (threadRuntime.aborted) return threadRuntime.defaultShort
    if (v == null) {
      threadRuntime.hadNull(true)
      threadRuntime.defaultShort
    } else {
      val s = v.trim
      try {
        if (s.isEmpty) {
          threadRuntime.defaultShort
        } else {
          var i = 0
          while (i < s.length) {
            if (!s.charAt(i).isDigit) return threadRuntime.defaultShort
            i += 1
          }
          s.toShort
        }
      } catch safely {
        case e: NumberFormatException => threadRuntime.defaultShort
      }
    }
  }

  @inline final def stringIntCoerce(v: String)(implicit threadRuntime: BrioThreadRuntime): Int = {
    if (threadRuntime.aborted) return threadRuntime.defaultInt
    if (v == null) {
      threadRuntime.hadNull(true)
      threadRuntime.defaultInt
    } else {
      val s = v.trim
      try {
        if (s.isEmpty) {
          threadRuntime.defaultInt
        } else {
          var i = 0
          while (i < s.length) {
            if (!s.charAt(i).isDigit) return threadRuntime.defaultInt
            i += 1
          }
          s.toInt
        }
      } catch safely {
        case e: NumberFormatException => threadRuntime.defaultInt
      }
    }
  }

  @inline final def stringLongCoerce(v: String)(implicit threadRuntime: BrioThreadRuntime): Long = {
    if (threadRuntime.aborted) return threadRuntime.defaultLong
    if (v == null) {
      threadRuntime.hadNull(true)
      threadRuntime.defaultLong
    } else {
      val s = v.trim
      try {
        if (s.isEmpty) {
          threadRuntime.defaultLong
        } else {
          var i = 0
          while (i < s.length) {
            if (!s.charAt(i).isDigit) return threadRuntime.defaultLong
            i += 1
          }
          s.toLong // TODO performance pig
        }
      } catch safely {
        case e: NumberFormatException => threadRuntime.defaultLong
      }
    }
  }

  @inline final def stringDoubleCoerce(v: String)(implicit threadRuntime: BrioThreadRuntime): Double = {
    if (threadRuntime.aborted) return threadRuntime.defaultDouble
    if (v == null) {
      threadRuntime.hadNull(true)
      threadRuntime.defaultDouble
    } else {
      val s = v.trim
      try {
        if (s.isEmpty) {
          threadRuntime.defaultDouble
        } else {
          s.toDouble
        }
      } catch safely {
        case e: NumberFormatException => threadRuntime.defaultDouble
      }
    }
  }


  @inline final def intDoubleCoerce(v: Int)(implicit threadRuntime: BrioThreadRuntime): Double = {
    if (threadRuntime.aborted) return threadRuntime.defaultDouble
    v.toDouble
  }

  @inline final def longDoubleCoerce(v: Long)(implicit threadRuntime: BrioThreadRuntime): Double = {
    if (threadRuntime.aborted) return threadRuntime.defaultDouble
    v.toDouble
  }

  @inline final def byteDoubleCoerce(v: Byte)(implicit threadRuntime: BrioThreadRuntime): Double = {
    if (threadRuntime.aborted) return threadRuntime.defaultDouble
    v.toDouble
  }

  @inline final def shortDoubleCoerce(v: Short)(implicit threadRuntime: BrioThreadRuntime): Double = {
    if (threadRuntime.aborted) return threadRuntime.defaultDouble
    v.toDouble
  }

}
