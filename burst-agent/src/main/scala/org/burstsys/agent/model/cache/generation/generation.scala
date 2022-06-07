/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.cache

import org.burstsys.agent.api.BurstQueryCacheGeneration
import org.burstsys.agent.model.cache.generation.key._
import org.burstsys.agent.model.cache.generation.metrics._
import org.burstsys.agent.model.cache.slice.state._
import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.metadata.model
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource

import scala.language.implicitConversions

package object generation {

  implicit def thriftToFabricCacheGeneration(a: BurstQueryCacheGeneration): FabricGeneration = {
    val datasource = model.datasource.FabricDatasource(a.identity.domainKey, a.identity.viewKey, a.identity.generationClock)
    val metrics = a.metrics: FabricGenerationMetrics
    FabricGeneration(datasource, metrics)
  }

  implicit def fabricToThriftCacheGeneration(a: FabricGeneration): BurstQueryCacheGeneration =
    BurstQueryCacheGeneration(
      identity = FabricGenerationKey().init(a.datasource.view),
      state = a.state,
      metrics = a.generationMetrics
    )

}
