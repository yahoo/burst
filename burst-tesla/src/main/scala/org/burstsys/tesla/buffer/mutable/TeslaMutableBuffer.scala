/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer.mutable

import java.nio.ByteBuffer

import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block._
import org.burstsys.tesla.buffer.access.{TeslaBufferReadAccessors, TeslaBufferWriteAccessors}
import org.burstsys.tesla.buffer.state.TeslaMutableBufferState
import org.burstsys.tesla.buffer.{TeslaBufferReader, TeslaBufferWriter}
import org.burstsys.tesla.pool.TeslaPoolId

import scala.language.implicitConversions

/**
 *
 */
trait TeslaMutableBuffer extends Any with TeslaBufferReader with TeslaBufferWriter with TeslaBlockPart {
  def initialize(id: TeslaPoolId): Unit

  def reset: TeslaMutableBuffer

  def toBytes: Array[Byte]

  def loadBytes(bytes: Array[Byte]): Unit

  def loadBytes(buffer: ByteBuffer): Unit

  def loadBytes(source: TeslaMemoryPtr, size: TeslaMemorySize): Unit

  def isNullBuffer: Boolean
}

/**
 * Mutable Mem Buffer embedded into a Memory Block
 */
final case
class TeslaMutableBufferAnyVal(blockPtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with TeslaMutableBufferState
  with TeslaMutableBuffer  {

  override def currentMemorySize: TeslaMemoryOffset = ??? // TODO

}
