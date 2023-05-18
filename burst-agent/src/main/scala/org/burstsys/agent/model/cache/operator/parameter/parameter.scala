/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.cache.operator

import org.burstsys.agent.api.BurstQueryCacheParamValue.{BoolVal, DoubleVal, LongVal}
import org.burstsys.agent.api.BurstQueryCacheParameter._
import org.burstsys.agent.api.{BurstQueryCacheOperationParameter, BurstQueryCacheParameter}
import org.burstsys.fabric.wave.data.model.ops._

import scala.language.implicitConversions

package object parameter {

  implicit def longToThriftLongVal(l: Long): LongVal = LongVal(l)

  implicit def doubleToThriftDoubleVal(d: Double): DoubleVal = DoubleVal(d)

  implicit def boolToThriftBoolVal(b: Boolean): BoolVal = BoolVal(b)

  implicit def thriftToFabricCacheParameter(p: BurstQueryCacheOperationParameter): FabricCacheOpParameter = p.value match {
    case LongVal(lVal) => FabricCacheOpParameter(p.name, p.relation, lVal = lVal)
    case DoubleVal(dVal) => FabricCacheOpParameter(p.name, p.relation, dVal = dVal)
    case BoolVal(bVal) => FabricCacheOpParameter(p.name, p.relation, bVal = bVal)
    case _ => ???
  }

  implicit def fabricToThriftCacheParameter(p: FabricCacheOpParameter): BurstQueryCacheOperationParameter = p.name match {
    case FabricCacheByteCount => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheItemCount => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheSliceCount => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheRegionCount => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheColdLoadAt => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheColdLoadTook => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheWarmLoadAt => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheWarmLoadTook => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheWarmLoadCount => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheSizeSkew => BurstQueryCacheOperationParameter(p.name, p.relation, p.dVal)
    case FabricCacheTimeSkew => BurstQueryCacheOperationParameter(p.name, p.relation, p.dVal)
    case FabricCacheItemSize => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheItemVariation => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheLoadInvalid => BurstQueryCacheOperationParameter(p.name, p.relation, p.bVal)
    case FabricCacheEarliestLoadAt => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheRejectedItemCount => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCachePotentialItemCount => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case FabricCacheSuggestedSampleRate => BurstQueryCacheOperationParameter(p.name, p.relation, p.dVal)
    case FabricCacheSuggestedSliceCount => BurstQueryCacheOperationParameter(p.name, p.relation, p.lVal)
    case _ => ???
  }

  implicit def thriftToFabricCacheParameterName(p: BurstQueryCacheParameter): FabricCacheOpParameterName = p match {
    case ByteCount => FabricCacheByteCount
    case ItemCount => FabricCacheItemCount
    case SliceCount => FabricCacheSliceCount
    case RegionCount => FabricCacheRegionCount
    case ColdLoadAt => FabricCacheColdLoadAt
    case ColdLoadTook => FabricCacheColdLoadTook
    case WarmLoadAt => FabricCacheWarmLoadAt
    case WarmLoadTook => FabricCacheWarmLoadTook
    case WarmLoadCount => FabricCacheWarmLoadCount
    case SizeSkew => FabricCacheSizeSkew
    case TimeSkew => FabricCacheTimeSkew
    case ItemSize => FabricCacheItemSize
    case ItemVariation => FabricCacheItemVariation
    case LoadInvalid => FabricCacheLoadInvalid
    case EarliestLoadAt => FabricCacheEarliestLoadAt
    case RejectedItemCount => FabricCacheRejectedItemCount
    case PotentialItemCount => FabricCachePotentialItemCount
    case SuggestedSampleRate => FabricCacheSuggestedSampleRate
    case SuggestedSliceCount => FabricCacheSuggestedSliceCount
    case _ => ???
  }

  implicit def fabricCacheParameterNameToThrift(p: FabricCacheOpParameterName): BurstQueryCacheParameter = p match {
    case FabricCacheByteCount => ByteCount
    case FabricCacheItemCount => ItemCount
    case FabricCacheSliceCount => SliceCount
    case FabricCacheRegionCount => RegionCount
    case FabricCacheColdLoadAt => ColdLoadAt
    case FabricCacheColdLoadTook => ColdLoadTook
    case FabricCacheWarmLoadAt => WarmLoadAt
    case FabricCacheWarmLoadTook => WarmLoadTook
    case FabricCacheWarmLoadCount => WarmLoadCount
    case FabricCacheSizeSkew => SizeSkew
    case FabricCacheTimeSkew => TimeSkew
    case FabricCacheItemSize => ItemSize
    case FabricCacheItemVariation => ItemVariation
    case FabricCacheLoadInvalid => LoadInvalid
    case FabricCacheEarliestLoadAt => EarliestLoadAt
    case FabricCacheRejectedItemCount => RejectedItemCount
    case FabricCachePotentialItemCount => PotentialItemCount
    case FabricCacheSuggestedSampleRate => SuggestedSampleRate
    case FabricCacheSuggestedSliceCount => SuggestedSliceCount
    case _ => ???
  }
}
