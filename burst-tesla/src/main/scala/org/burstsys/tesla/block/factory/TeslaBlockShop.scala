/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.block.factory

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockBuilder}
import org.burstsys.tesla.part.TeslaPartShop

/**
  * ==Block Pools==
  * For Block Pools we have the following requirements:
  * <ul>
  * <li>provide very high performance path for individual burst memory worker threads to allocate
  * and deallocate blocks within the thread's path i.e. no synchronization required.
  * We do this by having a per thread allocation pool
  * </li>
  * <li>provide a slower operation where one thread allocates and
  * another thread transparently frees back to the allocating pool via the same API.
  * We look at the block being freed, and if it comes from that thread's pool we simply return it
  * without any need for sync. If it is from another pool, we cross to that pool and return it which
  * requires synchronization.
  * </li>
  * <li>allocate memory only once to avoid the performance hit of repeated alloc and free
  * operations. This means we never free memory during the lifetime of the VM.
  * We limit the overall amount of memory allowed but all operations must be ok with that.
  * </li>
  * <li>Handle only a fixed set of block sizes i.e. we can set up sub pools of fixed sizes with
  * pool grabs choosing the equal or larger size sub pool to satisfy the request.
  * </li>
  * <li>All block sizes
  * are page size quanta (and page aligned?). There is maximum size block that can be configured.
  * This causes some loss of memory efficiency but these pools are mostly for small size and quantity pools.
  * </li>
  * <li>We want to carefully monitor stats for various pools in order to understand how efficiently we are doing
  * all this.
  * </li>
  * <li>Note this is ''not'' designed for huge quantity/size memory pools. It is for per thread working memory
  * chunks for things like zap maps/routes and melding
  * </li>
  * </ul>
  */
trait TeslaBlockShop extends TeslaPartShop[TeslaBlock, TeslaBlockBuilder] {

  def partName: String = block.partName

  /**
    *
    * @param byteSize
    * @return
    */
  def grabBlock(byteSize: TeslaMemorySize): TeslaBlock

  /**
    *
    * @param block
    */
  def releaseBlock(block: TeslaBlock): Unit


}
