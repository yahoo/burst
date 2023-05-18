/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model

package object ops {

  sealed case class FabricCacheManageOp(code: Int)

  object FabricCacheSearch extends FabricCacheManageOp(1)

  /** Perform an evict (remove from from memory) */
  object FabricCacheEvict extends FabricCacheManageOp(2)

  /** Perform a flush (remove from persistent storage) */
  object FabricCacheFlush extends FabricCacheManageOp(3)


  /**
   * please comment
   */
  sealed case class FabricCacheOpRelation(code: Int)

  object FabricCacheLT extends FabricCacheOpRelation(1)

  object FabricCacheGT extends FabricCacheOpRelation(2)

  object FabricCacheEQ extends FabricCacheOpRelation(3)


  /**
   * please comment
   */
  sealed case class FabricCacheOpParameterName(code: Int)

  object FabricCacheByteCount extends FabricCacheOpParameterName(1)

  object FabricCacheItemCount extends FabricCacheOpParameterName(2)

  object FabricCacheSliceCount extends FabricCacheOpParameterName(3)

  object FabricCacheRegionCount extends FabricCacheOpParameterName(4)

  object FabricCacheColdLoadAt extends FabricCacheOpParameterName(5)

  object FabricCacheColdLoadTook extends FabricCacheOpParameterName(6)

  object FabricCacheWarmLoadAt extends FabricCacheOpParameterName(7)

  object FabricCacheWarmLoadTook extends FabricCacheOpParameterName(8)

  object FabricCacheWarmLoadCount extends FabricCacheOpParameterName(9)

  object FabricCacheSizeSkew extends FabricCacheOpParameterName(10)

  object FabricCacheTimeSkew extends FabricCacheOpParameterName(11)

  object FabricCacheItemSize extends FabricCacheOpParameterName(12)

  object FabricCacheItemVariation extends FabricCacheOpParameterName(13)

  object FabricCacheLoadInvalid extends FabricCacheOpParameterName(14)

  object FabricCacheEarliestLoadAt extends FabricCacheOpParameterName(15)

  object FabricCacheRejectedItemCount extends FabricCacheOpParameterName(16)

  object FabricCachePotentialItemCount extends FabricCacheOpParameterName(17)

  object FabricCacheSuggestedSampleRate extends FabricCacheOpParameterName(18)

  object FabricCacheSuggestedSliceCount extends FabricCacheOpParameterName(19)


}
