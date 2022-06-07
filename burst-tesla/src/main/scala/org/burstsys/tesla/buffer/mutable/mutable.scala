/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer

import org.burstsys.tesla.TeslaTypes._

import scala.language.implicitConversions

package object mutable {

  implicit def longToBurstMutableBuffer(data: TeslaMemoryPtr): TeslaMutableBuffer = TeslaMutableBufferAnyVal(data)

  implicit def mutableBufferToLong(buffer: TeslaMutableBuffer): TeslaMutableBuffer = buffer.blockPtr

  final
  val SizeofMutableBufferHeader: TeslaMemorySize = SizeOfInteger + SizeOfInteger + SizeOfInteger

  final
  val nullMutableBuffer:TeslaMutableBuffer = TeslaMutableBufferAnyVal(TeslaNullMemoryPtr)

  final
  val endMarkerMutableBuffer:TeslaMutableBuffer = TeslaMutableBufferAnyVal(TeslaEndMarkerMemoryPtr)

}
