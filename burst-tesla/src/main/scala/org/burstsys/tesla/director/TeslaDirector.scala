/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.director

import java.nio.ByteBuffer

import org.burstsys.tesla.TeslaTypes
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize, TeslaNullMemoryPtr}
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal}
import org.burstsys.tesla.part.TeslaPart
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.tesla.offheap

/**
  * a tesla part used for routines that want to bridge into [[sun.nio.ch.DirectBuffer]] worlds
  */
trait TeslaDirector extends Any with TeslaPart {

  /**
    * resource pool id
    *
    * @return
    */
  def poolId: TeslaPoolId

  /**
    * a ptr to the payload of this director
    *
    * @return
    */
  def payloadPtr: TeslaMemoryPtr

  /**
    * max available memory for payload
    * @return
    */
  def maxAvailableMemory: TeslaMemorySize


  def blockBasePtr: TeslaMemoryPtr

  //////////////////////////////////////////////////////////////////////////////////////////
  // life cycle
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
    * initialize this reusable part
    *
    * @return
    */
  @inline
  def initialize(id: TeslaPoolId, size: TeslaMemorySize): TeslaDirector

  /**
    * reset this reusable part
    *
    * @return
    */
  @inline
  def reset(size: TeslaMemorySize): TeslaDirector

  /**
    * get a direct [[ByteBuffer]] from this tesla memory
    *
    * @return
    */
  @inline
  def directBuffer: ByteBuffer

}

object TeslaDirector {

  def apply(blockBasePtr: TeslaMemoryPtr = TeslaNullMemoryPtr, maxSize: TeslaMemorySize = 0): TeslaDirector =
    TeslaDirectorContext(blockBasePtr: TeslaMemoryPtr, maxSize: TeslaMemorySize)

}

private final case
class TeslaDirectorContext(blockBasePtr: TeslaMemoryPtr, maxSize: TeslaMemorySize) extends AnyRef with TeslaDirector {

  //////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _poolId: TeslaPoolId = 0

  private[this]
  var _currentSize: TeslaMemorySize = 0

  private[this]
  val _directBuffer: ByteBuffer = { // we set up a direct buffer at max size - we slice it below for real use
    offheap.directBuffer(payloadPtr, maxSize) order TeslaTypes.TeslaByteOrder
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def poolId: TeslaPoolId = _poolId

  @inline override
  def directBuffer: ByteBuffer = {
    // provide a slice on top of the main buffer that matches the current size
    val slice = _directBuffer.slice()
    slice.position(0).limit(_currentSize)
    slice
  }

  @inline override
  def payloadPtr: TeslaMemoryPtr = TeslaBlockAnyVal(blockBasePtr).dataStart

  @inline override
  def maxAvailableMemory: TeslaMemorySize = TeslaBlockAnyVal(blockBasePtr).dataSize

  //////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def initialize(id: TeslaPoolId, currentSize: TeslaMemorySize): TeslaDirector = {
    _poolId = id
    _currentSize = currentSize
    this
  }

  @inline override
  def reset(currentSize: TeslaMemorySize): TeslaDirector = {
    _currentSize = currentSize
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // TODO PART API
  //////////////////////////////////////////////////////////////////////////////////////////

  override def validateAndIncrementReferenceCount(msg: String): Unit = ???

  override def validateAndDecrementReferenceCount(msg: String): Unit = ???

  override def referenceCount: TeslaPoolId = ???

}
