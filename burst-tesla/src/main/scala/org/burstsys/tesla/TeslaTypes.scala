/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import java.nio.ByteOrder

object TeslaTypes {

  /**
   * https://en.wikipedia.org/wiki/Endianness
   * ==LITTLE_ENDIAN==
   * The least significant byte (LSB) value, 0Dh, is at the lowest address. The other bytes follow in
   * increasing order of significance. This is akin to right-to-left reading in hexadecimal order.
   * The Intel x86 and x86-64 series of processors use the little-endian format, and for this reason,
   * it is also known in the industry as the "Intel convention"
   */
  final val TeslaByteOrder = ByteOrder.LITTLE_ENDIAN

  // -------------- 2^63^ (9,223,372,036,854,775,808 byte) pointers ----------------------------------------

  /**
   * Memory pointer refer to absolute memory locations within the entire virtual
   * 2^63^ (9,223,372,036,854,775,808) byte byte memory space. Do not
   * confuses these with offsets below.
   */
  type TeslaMemoryPtr = Long

  // uninitialized memory pointer
  final val TeslaNullMemoryPtr: TeslaMemoryPtr = -1L

  // end marker memory pointer
  final val TeslaEndMarkerMemoryPtr: TeslaMemoryPtr = -2L

  // -------------------- 2^31^ (2,147,483,648 byte) memory blocks  ----------------------------------------

  /**
   * memory size within a 2^31^ byte memory block (not a pointer)
   */
  type TeslaMemorySize = Int

  /**
   * Offset into a 2^31^ bit (2,147,483,648 bytes) memory block. In most of our memory objects
   * we cannot support individual memory chunks greater than this number of bytes.
   */
  type TeslaMemoryOffset = Int

  // uninitialized memory offset
  final val TeslaNullOffset: TeslaMemoryOffset = -1

  final val SizeOfBoolean: TeslaMemorySize = 1
  final val SizeOfByte: TeslaMemorySize = 1
  final val SizeOfShort: TeslaMemorySize = 2
  final val SizeOfInteger: TeslaMemorySize = 4
  final val SizeOfLong: TeslaMemorySize = 8
  final val SizeOfDouble: TeslaMemorySize = 8

}
