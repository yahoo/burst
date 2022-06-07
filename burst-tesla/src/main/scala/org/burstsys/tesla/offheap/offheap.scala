/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.tesla.TeslaTypes._

import java.nio.ByteBuffer

package object offheap extends TeslaUnsafeCalls {
  private[offheap] var impl: TeslaUnsafeCalls = TeslaJ8or11Impl

  @inline lazy val nativeMemoryMax: Long = impl.nativeMemoryMax

  @inline lazy val arrayOffset: Int = impl.arrayOffset

  @inline def directBuffer(address: TeslaMemoryPtr, size: TeslaMemorySize): ByteBuffer = impl.directBuffer(address, size)

  @inline def releaseBuffer(niobuffer: ByteBuffer): Unit = impl.releaseBuffer(niobuffer)

  @inline def getByte(address: TeslaMemoryPtr): Byte = impl.getByte(address)

  @inline def putByte(address: TeslaMemoryPtr, x: Byte): Unit = impl.putByte(address, x)

  @inline def getShort(address: TeslaMemoryPtr): Short = impl.getShort(address)

  @inline def putShort(address: TeslaMemoryPtr, x: Short): Unit = impl.putShort(address, x)

  @inline def getInt(address: TeslaMemoryPtr): TeslaMemorySize = impl.getInt(address)

  @inline def putInt(address: TeslaMemoryPtr, x: TeslaMemorySize): Unit = impl.putInt(address, x)

  @inline def getLong(address: TeslaMemoryPtr): TeslaMemoryPtr = impl.getLong(address)

  @inline def putLong(address: TeslaMemoryPtr, x: TeslaMemoryPtr): Unit = impl.putLong(address, x)

  @inline def getDouble(address: TeslaMemoryPtr): Double = impl.getDouble(address)

  @inline def putDouble(address: TeslaMemoryPtr, x: Double): Unit = impl.putDouble(address, x)

  @inline def setMemory(address: TeslaMemoryPtr, bytes: TeslaMemoryPtr, value: Byte): Unit = impl.setMemory(address, bytes, value)

  @inline def copyMemory(srcAddress: TeslaMemoryPtr, destAddress: TeslaMemoryPtr, bytes: Long): Unit = impl.copyMemory(srcAddress, destAddress, bytes)

  @inline def copyMemory(source: TeslaMemoryPtr, destination: Array[Byte], byteCount: Long): Unit = impl.copyMemory(source, destination, byteCount)

  @inline def copyMemory(source: Array[Byte], destination: TeslaMemoryPtr, byteCount: Long): Unit = impl.copyMemory(source, destination, byteCount)

  @inline def arrayBaseOffset(arrayClass: Class[_]): TeslaMemorySize = impl.arrayBaseOffset(arrayClass)

  @inline def addressSize: TeslaMemorySize = impl.addressSize

  @inline def allocateMemory(bytes: Long): TeslaMemoryPtr = impl.allocateMemory(bytes)

  @inline def freeMemory(address: TeslaMemoryPtr): Unit = impl.freeMemory(address)

  @inline def pageSize: TeslaMemorySize = impl.pageSize
}
