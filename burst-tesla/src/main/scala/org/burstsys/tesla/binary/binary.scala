/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.TeslaMemoryPtr

/**
 * Binary encoding functions
 */
package object binary {

  @inline
  def swapIntOrder(i: Int): Int = i << 24 | i >> 8 & 0xff00 | i << 8 & 0xff0000 | i >>> 24

  @inline
  def fromIntBigEndian(value: Long, position: Int = 0): Array[Byte] = {
    val a = new Array[Byte](4)
    a(position) = ((value >>> 24) & 0xFF).toByte
    a(position + 1) = ((value >>> 16) & 0xFF).toByte
    a(position + 2) = ((value >>> 8) & 0xFF).toByte
    a(position + 3) = (value & 0xFF).toByte
    a
  }

  @inline
  def toIntBigEndian(bytes: Array[Byte], position: Int = 0): Int = {
    var v: Int = 0
    v |= (bytes(position) & 0xFF) << 24
    v |= (bytes(position + 1) & 0xFF) << 16
    v |= (bytes(position + 2) & 0xFF) << 8
    v |= (bytes(position + 3) & 0xFF)
    v
  }

  @inline
  def fromIntLittleEndian(value: Long, position: Int = 0): Array[Byte] = {
    val a = new Array[Byte](4)
    a(position + 3) = ((value >>> 24) & 0xFF).toByte
    a(position + 2) = ((value >>> 16) & 0xFF).toByte
    a(position + 1) = ((value >>> 8) & 0xFF).toByte
    a(position) = (value & 0xFF).toByte
    a
  }

  @inline
  def toIntLittleEndian(bytes: Array[Byte], position: Int = 0): Int = {
    var v: Int = 0
    v |= (bytes(position + 3) & 0xFF) << 24
    v |= (bytes(position + 2) & 0xFF) << 16
    v |= (bytes(position + 1) & 0xFF) << 8
    v |= (bytes(position) & 0xFF)
    v
  }

  def printBytes(sourcePtr: TeslaMemoryPtr, size: Long, rowLength: Int = 10): String = {
    val builder = new StringBuilder
    var indent = 0
    var i = 0
    while (i < size) {
      val value = tesla.offheap.getByte(sourcePtr + i)
      builder ++= f"$value%02X"
      indent += 1
      if (indent >= rowLength) {
        builder += '\n'
        indent = 0
      } else {
        builder += ' '
      }
      i += 1
    }
    builder.toString
  }

}
