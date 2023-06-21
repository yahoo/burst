/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.part

import org.burstsys.tesla.part
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.instrument.prettyByteSizeString
import org.jctools.queues.MpmcArrayQueue

import java.util
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

/**
 * [[org.burstsys.tesla.part.TeslaPartPool]] instances are created by and hosted in
 * [[org.burstsys.tesla.part.factory.TeslaPartFactory]]
 * instances specific to a single part, and are themselves host for alloc/free operations on a single
 *
 * @tparam PoolPart
 */
trait TeslaPartPool[PoolPart] extends AnyRef {

  /////////////////////////////////////////
  // Subtype implementation
  /////////////////////////////////////////

  def partName: String

  /**
   * the chunk size of this pool
   *
   * @return
   */
  def partByteSize: Int

  /**
   * how many __unused__ parts do we need to be able to keep track of?
   *
   * @return
   */
  def partQueueSize: Int = defaultPartsQueueSize

  /**
   * its important to keep careful track of pool ids so we can return things to
   * the right place with a minimum of synchronization
   *
   * @return
   */
  def poolId: TeslaPoolId

  /**
   * subtype specific implementation of freeing of a part
   *
   * @return
   */
  def freePart(part: PoolPart): Long

  /////////////////////////////////////////
  // API
  /////////////////////////////////////////

  /**
   * parts currently allocated (idle and in-use)
   *
   * @return
   */
  final
  def partsAllocated: Int = _partsAllocated.get

  /**
   * mark another allocation
   */
  final
  def incrementPartsAllocated(): Unit = _partsAllocated.incrementAndGet()

  /**
   * mark another part freed
   */
  final
  def decrementPartsAllocated(): Unit = _partsAllocated.decrementAndGet()

  final
  def partsInUse: Int = _partsInUse.get

  /**
   * mark another part in use
   */
  final
  def incrementPartsInUse(): Unit = {
    val newSize = _partsInUse.incrementAndGet()
    val oldSize = _maxPartsAllocated.get
    if (part.log.isTraceEnabled()) {
      if (newSize > oldSize) {
        _maxPartsAllocated.set(newSize)
        if (newSize % 500 == 0) {
          part.log info burstStdMsg(
            s"TeslaPartPool.incrementPartsInUse(partName='$partName', partByteSize=$partByteSize) maxPartsAllocated=${
              _maxPartsAllocated.get()
            }"
          )
        }
      }

    }
  }

  /**
   * mark another part returned
   */
  final
  def decrementPartsInUse(): Unit = {
    _partsInUse.decrementAndGet()
  }

  /**
   * mark another part in-use
   */
  final
  def markPartGrabbed(): Unit = _lastTimePartGrabbed.set(System.nanoTime)

  /**
   * LRU stats for time since last usage
   *
   * @return
   */
  final
  def nanosSinceLastPartGrab(): Long = System.nanoTime - _lastTimePartGrabbed.get

  /////////////////////////////////////////
  // State
  /////////////////////////////////////////

  /**
   * slick `off heap - lock free queue`...
   * this is '''lazy''' because many different [[TeslaPartPool]] ''sizes'' within a
   * [[org.burstsys.tesla.part.factory.TeslaPartFactory]] don't get used
   * and thus we save on heap
   */
  final lazy val partQueue: util.Queue[PoolPart] = new MpmcArrayQueue[PoolPart](partQueueSize)

  private[this]
  val _maxPartsAllocated = new AtomicInteger

  private[this]
  val _partsInUse = new AtomicInteger

  private[this]
  val _partsAllocated = new AtomicInteger

  private[this]
  val _lastTimePartGrabbed = new AtomicLong

  /////////////////////////////////////////
  // Implementation
  /////////////////////////////////////////

  // log the creation of this part pool
  if (part.log.isTraceEnabled)
    part.log trace
      s"TeslaPartPool.init partName='$partName', pool=p$poolId, binding='${
        Thread.currentThread.getName
      }', size=${
        prettyByteSizeString(partByteSize)
      }"

  /**
   * Free all parts in this pool up to  '''maxPartsFreedInOneRun'''
   *
   * @return the bytes freed
   */
  final
  def freeAllUnusedParts: (Long, Long) = {
    if (_partsInUse.get == 0 && _partsAllocated.get == 0) return (0, 0)
    var partsFreed = 0
    var bytesFreed: Long = 0L
    partQueue synchronized {
      var i = 0
      var continue = true
      while (i < maxPartsFreedInOneRun && continue) {
        partQueue.poll match {
          case null =>
            continue = false
          case part =>
            decrementPartsAllocated()
            partsFreed += 1
            bytesFreed += freePart(part)
        }
        i += 1
      }
    }
    (partsFreed, bytesFreed)
  }

}
