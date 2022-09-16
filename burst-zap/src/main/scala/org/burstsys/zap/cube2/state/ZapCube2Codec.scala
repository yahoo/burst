/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.state

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.tesla.offheap
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.errors.{VitalsException, safely}

/**
 * Kryo serialization and deserialization for [[org.burstsys.zap.cube2.ZapCube2]]
 */
trait ZapCube2Codec extends Any with ZapCube2State {

  final
  def write(k: Kryo, out: Output): Unit = {
    try {
      out writeByte dimCount
      out writeByte aggCount
      out writeInt bucketsStart
      out writeInt bucketsCount
      out writeInt cursorStart
      out writeInt cursorRow
      out writeBoolean cursorUpdated
      out writeInt pivotStart
      out writeInt rowsCount
      out writeInt resizeCount
      out writeInt rowSize
      out writeInt rowsStart
      out writeInt rowsEnd
      out writeBoolean rowsLimited
      out writeInt cursorRow

      // buckets
      var b = 0
      while (b < bucketsCount) {
        out writeLong offheap.getLong(basePtr + bucketsStart + (b * SizeOfLong))
        b += 1
      }

      // cursor
      cursor.write(k, out)

      // pivot
      pivot.write(k, out)

      // rows TODO ADD IN SNAPPY COMPRESSION FOR LARGE ROW SETS??
      var rowIndex = 0
      while (rowIndex < rowsCount) {
        row(rowIndex).write(k, out)
        rowIndex += 1
      }
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  final
  def read(k: Kryo, in: Input): Unit = {
    try {
      dimCount = in.readByte
      aggCount = in.readByte
      bucketsStart = in.readInt
      bucketsCount = in.readInt
      cursorStart = in.readInt
      cursorRow = in.readInt
      cursorUpdated = in.readBoolean
      pivotStart = in.readInt
      rowsCount = in.readInt
      resizeCount = in.readInt
      rowSize = in.readInt
      rowsStart = in.readInt
      rowsEnd = in.readInt
      rowsLimited = in.readBoolean
      cursorRow = in.readInt

      // buckets
      var b = 0
      while (b < bucketsCount) {
        offheap.putLong(basePtr + bucketsStart + (b * SizeOfLong), in.readLong)
        b += 1
      }

      // cursor
      cursor.read(k, in)

      // pivot
      pivot.read(k, in)

      // rows TODO ADD IN SNAPPY COMPRESSION FOR LARGE ROW SETS??
      var rowIndex = 0
      while (rowIndex < rowsCount) {
        row(rowIndex).read(k, in)
        rowIndex += 1
      }
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
