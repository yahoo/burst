/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.block.TeslaBlockAnyVal
import org.burstsys.tesla.buffer
import org.burstsys.tesla.buffer.TeslaBufferReporter
import org.burstsys.tesla.buffer.mutable.{TeslaMutableBuffer, TeslaMutableBufferAnyVal}
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.logging.burstStdMsg

/**
 * parts pool for tesla buffers
 */
final
case class TeslaBufferPool(poolId: TeslaPoolId, partByteSize: TeslaMemorySize)
  extends TeslaPartPool[TeslaMutableBuffer] with TeslaBufferShop {

  override val partQueueSize: Int = 5e5.toInt // 5e5

  @inline override
  def grabBuffer(size: TeslaMemorySize): TeslaMutableBuffer = {
    markPartGrabbed()
    TeslaBufferReporter.grab()
    val part = partQueue.poll match {
      case null =>
        incrementPartsAllocated()
        TeslaBufferReporter.alloc(partQueueSize)
        val bp = tesla.block.factory.grabBlock(size).blockBasePtr
        val allocatedBuffer = TeslaMutableBufferAnyVal(bp)
        allocatedBuffer.initialize(poolId)
        if (buffer.log.isTraceEnabled) {
          buffer.log trace burstStdMsg(s"allocated new buffer=${allocatedBuffer.basePtr} inuse=$partsInUse allocated=$partsAllocated")
        }
        allocatedBuffer
      case p =>
        if (buffer.log.isTraceEnabled) {
          buffer.log trace burstStdMsg(s"allocated pooled buffer=${p.basePtr} inuse=$partsInUse allocated=$partsAllocated")
        }
        p.reset
    }
    incrementPartsInUse()
    part
  }

  @inline override
  def releaseBuffer(r: TeslaMutableBuffer): Unit = {
    decrementPartsInUse()
    TeslaBufferReporter.release()
    if (buffer.log.isTraceEnabled) {
      buffer.log trace burstStdMsg(s"returning buffer=${r.basePtr} to pool inuse=$partsInUse allocated=$partsAllocated")
    }
    partQueue add r.blockPtr
  }

  @inline override
  def freePart(part: TeslaMutableBuffer): TeslaMemoryPtr = {
    TeslaBufferReporter.free(partQueueSize)
    val block = TeslaBlockAnyVal(part.blockBasePtr)
    if (buffer.log.isTraceEnabled) {
      buffer.log trace burstStdMsg(s"freeing buffer=${part.basePtr} inuse=$partsInUse allocated=$partsAllocated")
    }
    tesla.block.factory.releaseBlock(block)
    block.dataSize
  }
}
