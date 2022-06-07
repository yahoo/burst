/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.cache.generation

import org.burstsys.agent.api.BurstQueryCacheGenerationMetrics
import org.burstsys.agent.model.cache.generation.key._
import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.data.model.slice.state.FabricDataCold

import scala.language.implicitConversions

package object metrics {

  implicit def fabricToThriftCacheMetrics(a: FabricGenerationMetrics): BurstQueryCacheGenerationMetrics =
    BurstQueryCacheGenerationMetrics(
      generationKey = a.generationKey,
      byteCount = a.byteCount,
      itemCount = a.itemCount,
      sliceCount = a.sliceCount,
      regionCount = a.regionCount,
      coldLoadAt = a.coldLoadAt,
      coldLoadTook = a.coldLoadTook,
      warmLoadAt = a.warmLoadAt,
      warmLoadTook = a.warmLoadTook,
      warmLoadCount = a.warmLoadCount,
      sizeSkew = a.sizeSkew,
      timeSkew = a.timeSkew,
      itemSize = a.itemSize,
      itemVariation = a.itemVariation,
      loadInvalid = a.loadInvalid,
      earliestLoadAt = a.earliestLoadAt,
      rejectedItemCount = a.rejectedItemCount,
      expectedItemCount = a.expectedItemCount,
      potentialItemCount = a.potentialItemCount,
      suggestedSampleRate = a.suggestedSampleRate,
      suggestedSliceCount = a.suggestedSliceCount
    )

  implicit def thriftToFabricGenerationMetrics(a: BurstQueryCacheGenerationMetrics): FabricGenerationMetrics =
    FabricGenerationMetrics().init(
      generationKey = a.generationKey,
      FabricDataCold,
      byteCount = a.byteCount,
      itemCount = a.itemCount,
      sliceCount = a.sliceCount,
      regionCount = a.regionCount,
      coldLoadAt = a.coldLoadAt,
      coldLoadTook = a.coldLoadTook,
      warmLoadAt = a.warmLoadAt,
      warmLoadTook = a.warmLoadTook,
      warmLoadCount = a.warmLoadCount,
      sizeSkew = a.sizeSkew,
      timeSkew = a.timeSkew,
      itemSize = a.itemSize,
      itemVariation = a.itemVariation,
      loadInvalid = a.loadInvalid,
      earliestLoadAt = a.earliestLoadAt,
      rejectedItemCount = a.rejectedItemCount,
      expectedItemCount = a.expectedItemCount,
      potentialItemCount = a.potentialItemCount,
      suggestedSampleRate = a.suggestedSampleRate,
      suggestedSliceCount = a.suggestedSliceCount
    )

}
