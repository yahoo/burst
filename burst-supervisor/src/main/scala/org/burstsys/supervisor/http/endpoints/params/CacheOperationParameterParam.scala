/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.endpoints.params

import org.burstsys.fabric.container.http.endpoints.params.GenericParam
import org.burstsys.fabric.wave.data.model.ops.FabricCacheByteCount
import org.burstsys.fabric.wave.data.model.ops.FabricCacheColdLoadAt
import org.burstsys.fabric.wave.data.model.ops.FabricCacheColdLoadTook
import org.burstsys.fabric.wave.data.model.ops.FabricCacheEQ
import org.burstsys.fabric.wave.data.model.ops.FabricCacheGT
import org.burstsys.fabric.wave.data.model.ops.FabricCacheItemCount
import org.burstsys.fabric.wave.data.model.ops.FabricCacheLT
import org.burstsys.fabric.wave.data.model.ops.FabricCacheOpParameter
import org.burstsys.fabric.wave.data.model.ops.FabricCacheRegionCount
import org.burstsys.fabric.wave.data.model.ops.FabricCacheSizeSkew
import org.burstsys.fabric.wave.data.model.ops.FabricCacheSliceCount
import org.burstsys.fabric.wave.data.model.ops.FabricCacheTimeSkew

import scala.language.implicitConversions

case class CacheOperationParameterParam(override val raw: String, override val value: Option[FabricCacheOpParameter]) extends GenericParam[FabricCacheOpParameter](raw, value)

object CacheOperationParameterParam {
  implicit def toCacheOpParam(p: GenericParam[FabricCacheOpParameter]): CacheOperationParameterParam = CacheOperationParameterParam(p.raw, p.value)

  def valueOf(param: String): CacheOperationParameterParam = {
    GenericParam.parse(param, fn = s => {
      val pieces = s.split("-")
      val relation = pieces(1) match {
        case "=" => FabricCacheEQ
        case ">" => FabricCacheGT
        case "<" => FabricCacheLT
        case _ => throw new IllegalStateException
      }
      pieces(0) match {
        case "i" => FabricCacheOpParameter(FabricCacheItemCount, relation, lVal = pieces.last.toLong)
        case "s" => FabricCacheOpParameter(FabricCacheSliceCount, relation, lVal = pieces.last.toLong)
        case "r" => FabricCacheOpParameter(FabricCacheRegionCount, relation, lVal = pieces.last.toLong)
        case "b" => FabricCacheOpParameter(FabricCacheByteCount, relation, lVal = pieces.last.toLong)
        case "cla" => FabricCacheOpParameter(FabricCacheColdLoadAt, relation, lVal = pieces.last.toLong)
        case "clt" => FabricCacheOpParameter(FabricCacheColdLoadTook, relation, lVal = pieces.last.toLong)
        case "t" => FabricCacheOpParameter(FabricCacheTimeSkew, relation, dVal = pieces.last.toDouble)
        case "z" => FabricCacheOpParameter(name = FabricCacheSizeSkew, relation = relation, dVal = pieces.last.toDouble)
        case _ => throw new IllegalStateException
      }
    })
  }
}

