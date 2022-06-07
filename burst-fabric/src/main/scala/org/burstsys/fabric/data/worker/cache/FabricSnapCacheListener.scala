/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.worker.cache

import org.burstsys.fabric.data.model.ops.FabricCacheOps
import org.burstsys.fabric.data.model.snap.FabricSnap

/**
 * Listener for [[FabricCacheOps]] events...
 */
trait FabricSnapCacheListener extends Any {

  /**
   * the cache is being started
   *
   * @param cache
   */
  def onSnapCacheStart(cache: FabricSnapCache): Unit = {}

  /**
   * the cache is being stopped
   *
   * @param cache
   */
  def onSnapCacheStop(cache: FabricSnapCache): Unit = {}

  /**
   * the cache is being tended
   *
   * @param cache
   */
  def onSnapCacheTend(cache: FabricSnapCache): Unit = {}

  /**
   * a slice is being evicted
   *
   * @param snap
   */
  def onSnapEvict(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {}

  /**
   * a slice is being flushed
   *
   * @param snap
   */
  def onSnapFlush(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {}

  /**
   * a slice is being erased
   *
   * @param snap
   */
  def onSnapErase(snap: FabricSnap): Unit = {}

  /**
   *
   * @param snap
   */
  def onSnapColdLoad(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {}

  /**
   *
   * @param snap
   */
  def onSnapWarmLoad(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {}

  /**
   *
   * @param snap
   */
  def onSnapHotLoad(snap: FabricSnap, elapsedNs: Long, bytes: Long): Unit = {}

  /**
   *
   * @param snap
   */
  def onSnapFailLoad(snap: FabricSnap): Unit = {}

  /**
   *
   * @param snap
   */
  def onSnapReslice(snap: FabricSnap): Unit = {}

}
