/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.worker.cache.internal

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.fabric.data.model.snap.{FabricSnap, _}
import org.burstsys.fabric.data.worker.cache.FabricSnapCache

import scala.collection.JavaConverters._
import scala.language.postfixOps

/**
 * storage/lookup of [[FabricSnap]] instances in the [[FabricSnapCache]]
 */
trait FabricSnapCacheMap extends AnyRef with FabricSnapCache {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final protected
  val _snapCache = new ConcurrentHashMap[FabricSlice, FabricSnap]

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final protected
  def clearCacheMap(): Unit = _snapCache.clear()

  final protected
  def allSnaps(): java.util.Iterator[FabricSnap] = _snapCache.values.iterator()

  final protected
  def +=(snap: FabricSnap): Unit = _snapCache.put(snap.slice, snap)

  final protected
  def -=(snap: FabricSnap): Unit = _snapCache.remove(snap.slice)

  final protected
  def get(slice: FabricSlice): Option[FabricSnap] = Option(_snapCache.get(slice))

  final override def coldSnapCount: Int = _snapCache.values.asScala.count(_.state == ColdSnap)

  final override def warmSnapCount: Int = _snapCache.values.asScala.count(_.state == WarmSnap)

  final override def hotSnapCount: Int = _snapCache.values.asScala.count(_.state == HotSnap)

  /**
   * snaps in [[HotSnap]] state - sorted by access time
   *
   * @return
   */
  final protected
  def evictCandidateSnaps: List[FabricSnap] =
    _snapCache.values.asScala.toList.filter(inEvictReadyState).sortBy(_.lastAccessTime)

  /**
   * snaps in [[HotSnap]] OR [[WarmSnap]] OR [[NoDataSnap]] state - sorted by access time
   *
   * @return
   */
  final protected
  def flushCandidateSnaps: List[FabricSnap] =
    _snapCache.values.asScala.toList.filter(inFlushReadyState).sortBy(_.lastAccessTime)

  /**
   * snaps in [[ColdSnap]] state - sorted by access time
   *
   * @return
   */
  final protected
  def eraseCandidateSnaps: List[FabricSnap] =
    _snapCache.values.asScala.toList.filter(inEraseReadyState).sortBy(_.lastAccessTime)

  /**
   * snaps in [[HotSnap]] state AND that have evict TTL expired - sorted by access time
   *
   * @return
   */
  final protected
  def evictTtlExpiredSnaps: List[FabricSnap] = evictCandidateSnaps.filter(_.evictTtlExpired)

  /**
   * snaps in  [[HotSnap]] OR [[WarmSnap]] OR [[NoDataSnap]] state AND that have flush TTL expired - sorted by access time
   *
   * @return
   */
  final protected
  def flushTtlExpiredSnaps: List[FabricSnap] = flushCandidateSnaps.filter(_.flushTtlExpired)

  /**
   * snaps in [[ColdSnap]] state AND that have erase TTL expired - sorted by access time
   *
   * @return
   */
  final protected
  def eraseTtlExpiredSnaps: List[FabricSnap] = eraseCandidateSnaps.filter(_.eraseTtlExpired)

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private def inEvictReadyState(s: FabricSnap): Boolean = s.state == HotSnap

  private def inFlushReadyState(s: FabricSnap): Boolean = s.state == HotSnap || s.state == WarmSnap || s.state == NoDataSnap

  private def inEraseReadyState(s: FabricSnap): Boolean = s.state == ColdSnap

}
