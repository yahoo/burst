/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.part.factory

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.part.{TeslaPartPool, maxPoolsPerPart}
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.tesla.thread
import org.burstsys.vitals.errors.VitalsException
import gnu.trove.map.hash.TIntObjectHashMap

import scala.reflect.ClassTag

/**
 * A [[TeslaPartFactory]] instance is host for a given part type that and its [[TeslaPartPool]] instances
 * <hr/>
 */
abstract class TeslaPartFactory[FactoryPart, PartPool <: TeslaPartPool[FactoryPart] : ClassTag]
  extends TeslaFactoryTender[FactoryPart, PartPool] with TeslaFactoryPrinter[FactoryPart, PartPool] {

  /**
   * This is the normal way we find block pools. This does not need to be
   * synchronized because its only used within a single thread
   */
  type TeslaPoolMap = TIntObjectHashMap[PartPool]

  final override def toString: String = s"$partName factory "

  ///////////////////////////////////////////////////////////////////////////////
  // private state
  ///////////////////////////////////////////////////////////////////////////////

  /**
   * the pool ids for this factory
   */
  private[this] final
  val _poolIdGenerator = new AtomicInteger(0)

  /**
   * This is an efficient way to find block pools when we do cross pool returns.
   * This does not need to be synchronized because its created at init and immutable after
   */
  private[this] final
  val _pools: Array[PartPool] = new Array[PartPool](maxPoolsPerPart)

  /**
   * There are two types of pools - a set of per thread pools for tesla.worker.threads
   */
  private[this] final
  val _threadMaps = new ThreadLocal[TeslaPoolMap]

  ///////////////////////////////////////////////////////////////////////////////
  // subtype implementation
  ///////////////////////////////////////////////////////////////////////////////

  /**
   * name used for logging clarity
   *
   * @return
   */
  def partName: String

  /**
   * the percentage of direct memory this pool can
   *
   * @return
   */
  final def poolSizeAsPercentOfDirectMemory: Double = 0.05

  /**
   * instantiate the specialized part pool for this part type
   *
   * @return
   */
  def instantiatePartPool(poolId: TeslaPoolId, size: TeslaMemorySize): PartPool

  ///////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////

  /**
   * the complete set of part pools
   *
   * @return
   */
  final
  def pools: Array[PartPool] = _pools.filter(_ != null)

  /**
   * does this factory have any currently in-use parts?
   *
   * @return
   */
  final
  def inUseParts: Int = pools.map(_.partsInUse).sum

  /**
   * free all unused parts in all pools
   *
   * @return count of freed parts
   */
  final
  def freeAllUnusedParts: Long = pools.map(_.freeAllUnusedParts._1).sum

  /**
   * get a pool by poolid - we check to see if its a plausible id
   *
   * @return
   */
  final
  def poolByPoolId(pid: TeslaPoolId): PartPool = {
    if (pid > _poolIdGenerator.get - 1 || pid < 0) {
      throw VitalsException(s"part with poolId=$pid not appropriate for pool ${_poolIdGenerator.get - 1}")
    }
    val p = _pools(pid)
    if (p == null) {
      throw VitalsException(s" null pool for poolId=$pid")
    }
    p
  }

  /**
   * return the per thread part pool
   *
   * @return
   */
  final
  def perThreadPartPool(bytes: Int): PartPool = {
    lazy val tag = s"TeslaPartFactory.perThreadPartPool(partName=$partName, bytes=$bytes)"
    if (!thread.worker.inTeslaWorkerThread) throw VitalsException(
      s"TESLA_NOT_WORKER_THREAD -- entrance to part pool must be worker thread! (${Thread.currentThread()} is not) $tag"
    )
    var map = _threadMaps.get
    if (map == null) {
      // we hit a fixed thread pool - it makes sense to give it the full thread bound pool array
      _threadMaps.set(instantiatePoolMap())
      map = _threadMaps.get
    }
    map.get(bytes) match {
      case null => throw VitalsException(s"TESLA_PART_POOL_NOT_FOUND $tag ")
      case p => p
    }
  }

  ///////////////////////////////////////////////////////////////////////////////
  // internals
  ///////////////////////////////////////////////////////////////////////////////

  /**
   * instantiate all the part pools of all the different chunk sizes
   *
   * @return
   */
  private
  def instantiatePoolMap(): TeslaPoolMap = {
    TeslaFactoryBoss.registerFactory(this)
    val map = new TeslaPoolMap
    // create one for each block size
    TeslaBlockSizes.blockSizes foreach {
      s =>
        val id = _poolIdGenerator.getAndIncrement
        val f = instantiatePartPool(id, s)
        _pools(id) = f
        map.put(s, f)
    }
    map
  }

}
