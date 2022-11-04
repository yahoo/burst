/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints

import org.burstsys.dash.application.GenericParam
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.ops._
import org.burstsys.vitals.logging._

import scala.language.implicitConversions

package object cache extends VitalsLogger {

}

case class GenerationKeyParam(override val raw: String, override val value: Option[FabricGenerationKey]) extends GenericParam[FabricGenerationKey](raw, value)

object GenerationKeyParam {
  implicit def toGenerationKeyParam(p: GenericParam[FabricGenerationKey]): GenerationKeyParam = GenerationKeyParam(p.raw, p.value)

  def valueOf(param: String): GenerationKeyParam = {
    GenericParam.parse(param, s => {
      val pieces = s.split("\\.")
      pieces.length match {
        case 1 => FabricGenerationKey(domainKey = pieces(0).toLong)
        case 2 => FabricGenerationKey(domainKey = pieces(0).toLong, viewKey = pieces(1).toLong)
        case 3 => FabricGenerationKey(pieces(0).toLong, pieces(1).toLong, pieces(2).toLong)
        case _ => throw new IllegalStateException
      }
    })
  }
}

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
