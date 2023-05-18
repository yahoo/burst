/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.endpoints.params

import org.burstsys.fabric.container.http.endpoints.params.GenericParam
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey

import scala.language.implicitConversions

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


