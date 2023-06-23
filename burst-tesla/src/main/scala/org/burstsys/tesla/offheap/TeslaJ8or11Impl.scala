/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.offheap

import jdk.internal.misc.SharedSecrets
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.errors._
import sun.misc.Unsafe

import java.lang.management.{BufferPoolMXBean, ManagementFactory}
import scala.jdk.CollectionConverters._
import sun.nio.ch.DirectBuffer

import java.lang.reflect.Field
import java.nio.ByteBuffer

object TeslaJ8or11Impl extends TeslaUnsafeCalls {
  final lazy val nativeMemoryMax: Long = {
    ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.find(_.getName.contains("direct")) match {
      case Some(p) =>
        p.getTotalCapacity
      case None =>
        Runtime.getRuntime.maxMemory()
    }
  }

  final val arrayOffset:Int  = accessor.arrayBaseOffset(classOf[Array[Byte]])

  def directBuffer(address: TeslaMemoryPtr, size: TeslaMemorySize): ByteBuffer = {
    //TeslaDirectBufferFactory.directBuffer(address, size)
    SharedSecrets.getJavaNioAccess.newDirectByteBuffer(address, size, null).order(TeslaByteOrder)
  }

  def releaseBuffer(niobuffer: ByteBuffer): Unit = {
    if (niobuffer != null && niobuffer.isDirect) {
      /**
       * <h1>Why?</h1>
       * this is a very unsafe way to make sure an byte buffer (specifically mmap() file direct buffer)
       * gets collected right away.
       * <h1>From the open jdk source...</h1>
       * General-purpose phantom-reference-based cleaners.
       * Cleaners are a lightweight and more robust alternative to finalization. They are lightweight because
       * they are not created by the VM and thus do not require a JNI upcall to be created, and because their
       * cleanup code is invoked directly by the reference-handler thread rather than by the finalizer thread.
       * They are more robust because they use phantom references, the weakest type of reference object, thereby
       * avoiding the nasty ordering problems inherent to finalization.
       * A cleaner tracks a referent object and encapsulates a thunk of arbitrary cleanup code. Some time after
       * the GC detects that a cleaner's referent has become phantom-reachable, the reference-handler thread will
       * run the cleaner. Cleaners may also be invoked directly; they are thread safe and ensure that they run
       * their thunks at most once.
       * Cleaners are not a replacement for finalization. They should be used only when the cleanup code is
       * extremely simple and straightforward. Nontrivial cleaners are inadvisable since they risk blocking
       * the reference-handler thread and delaying further cleanup and finalization.
       */
      accessor.invokeCleaner(niobuffer)
      /*
      val cleaner = niobuffer.asInstanceOf[DirectBuffer].cleaner
      if (cleaner != null) {
        cleaner.clean()
      }
      */
    }
  }

  @inline final
  private lazy val accessor: Unsafe = {
    try {
      Unsafe.getUnsafe
    } catch safely {
      case _: SecurityException =>
        try {
          val f: Field = classOf[Unsafe].getDeclaredField("theUnsafe")
          f.setAccessible(true)
          f.get(null).asInstanceOf[Unsafe]
        } catch safely {
          case _: NoSuchFieldException => null
          case _: IllegalAccessException => null
        }
    }
  }

  def getByte(address: TeslaMemoryPtr): Byte = accessor.getByte(address)

  def putByte(address: TeslaMemoryPtr, x: Byte): Unit = accessor.putByte(address, x)

  def getShort(address: TeslaMemoryPtr): Short = accessor.getShort(address)

  def putShort(address: TeslaMemoryPtr, x: Short): Unit = accessor.putShort(address, x)

  def getInt(address: TeslaMemoryPtr): TeslaMemorySize = accessor.getInt(address)

  def putInt(address: TeslaMemoryPtr, x: TeslaMemorySize): Unit = accessor.putInt(address, x)

  def getLong(address: TeslaMemoryPtr): TeslaMemoryPtr = accessor.getLong(address)

  def putLong(address: TeslaMemoryPtr, x: TeslaMemoryPtr): Unit = accessor.putLong(address, x)

  def getDouble(address: TeslaMemoryPtr): Double = accessor.getDouble(address)

  def putDouble(address: TeslaMemoryPtr, x: Double): Unit = accessor.putDouble(address, x)

  def setMemory(address: TeslaMemoryPtr, bytes: TeslaMemoryPtr, value: Byte): Unit = accessor.setMemory(address, bytes, value)

  def copyMemory(srcAddress: TeslaMemoryPtr, destAddress: TeslaMemoryPtr, bytes: Long): Unit = accessor.copyMemory(srcAddress, destAddress, bytes)

  def copyMemory(source: TeslaMemoryPtr, destination: Array[Byte], byteCount: Long): Unit = accessor.copyMemory(null, source, destination, arrayOffset, byteCount)

  def copyMemory(source: Array[Byte], destination: TeslaMemoryPtr, byteCount: Long): Unit = accessor.copyMemory(source, arrayOffset, null, destination, byteCount)

  def arrayBaseOffset(arrayClass: Class[_]): TeslaMemorySize = accessor.arrayBaseOffset(arrayClass)

  def addressSize: TeslaMemorySize = accessor.addressSize()

  def allocateMemory(bytes: Long): TeslaMemoryPtr = accessor.allocateMemory(bytes)

  def freeMemory(address: TeslaMemoryPtr): Unit = accessor.freeMemory(address)

  def pageSize: TeslaMemorySize = accessor.pageSize()
}
