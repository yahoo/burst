/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.offheap

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}

import java.nio.ByteBuffer

trait TeslaUnsafeCalls {
  // burst specific unsafe
  def arrayOffset: Int

  def nativeMemoryMax: Long

  def directBuffer(address: TeslaMemoryPtr, size: TeslaMemorySize): ByteBuffer

  def releaseBuffer(niobuffer: ByteBuffer): Unit

  // generic unsafe calls

  def getByte(address: Long): Byte

  def putByte(address: Long, x: Byte): Unit

  def getShort(address: Long): Short

  def putShort(address: Long, x: Short): Unit

  def getInt(address: Long): Int

  def putInt(address: Long, x: Int): Unit

  def getLong(address: Long): Long

  def putLong(address: Long, x: Long): Unit

  def getDouble(address: Long): Double

  def putDouble(address: Long, x: Double): Unit

  def allocateMemory(bytes: Long): Long

  def setMemory(address: Long, bytes: Long, value: Byte): Unit

  def copyMemory(srcAddress: Long, destAddress: Long, bytes: Long): Unit

  def copyMemory(source: Long, destination: Array[Byte], byteCount: Long): Unit

  def copyMemory(source: Array[Byte], destination: Long, byteCount: Long): Unit

  def freeMemory(address: Long): Unit

  def arrayBaseOffset(arrayClass: Class[_]): Int

  def addressSize: Int

  def pageSize: Int
}
