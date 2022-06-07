/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.cache

import org.burstsys.agent.api.{BurstQueryCacheSlice, BurstQuerySliceKey}
import org.burstsys.agent.model.cache.generation.metrics._
import org.burstsys.agent.model.cache.slice.state._
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata

import scala.language.implicitConversions

package object slice {

  implicit def fabricToThriftCacheSlice(slice: FabricSliceMetadata): BurstQueryCacheSlice =
    BurstQueryCacheSlice(
      identity = BurstQuerySliceKey(sliceId = slice.sliceKey, hostname = slice.hostname),
      state = slice.state,
      metrics = slice.generationMetrics
    )

}
