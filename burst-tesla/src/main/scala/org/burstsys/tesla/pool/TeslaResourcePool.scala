/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.pool

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong

import org.burstsys.vitals.errors._

/**
 * Common support for pools of resources that are handled in a high-performance, and
 * ''easy to find leaks'' way. Currently this is only used for heavy weight on-heap objects such as gathers.
 *
 * @deprecated dependencies should prolly should go to the off-heap 'part' based factory model
 */
final case
class TeslaResourcePool[T <: TeslaPooledResource](poolName: String, poolId: Int, maxAllocations: Int) {

  private[this]
  lazy val inUseCount = new AtomicLong

  private[this]
  lazy val resourcePool: ArrayBlockingQueue[T] = new ArrayBlockingQueue[T](maxAllocations + 1)

  def poolGrab: T = {
    inUseCount.incrementAndGet
    resourcePool.poll match {
      case null => null.asInstanceOf[T]
      case m => m
    }
  }

  def printPool(verb: String): String = {
    s"$verb in_use = ${inUseCount.get}, pool_size = ${resourcePool.size} / $maxAllocations"
  }

  def poolRelease(resource: T): Unit = {
    if (resource.poolId != poolId) {
      val msg = s"$this.poolRelease resource #${
        resource.poolId
      } not from pool=$poolId!: ${printPool("ERROR")}"
      throw new RuntimeException(msg)
    }
    inUseCount.decrementAndGet()
    if (inUseCount.get < 0) {
      val msg = s"$this.poolRelease no resources allocated!: \n${printPool("ERROR")}"
      throw new RuntimeException(msg)
    }
    try {
      resourcePool put resource
    } catch safely {
      case t: Throwable =>
        throw new RuntimeException(s"$resourcePool", t)
    }
  }

  override def toString: String = s"TeslaResourcePool('$poolName', $poolId)"
}
