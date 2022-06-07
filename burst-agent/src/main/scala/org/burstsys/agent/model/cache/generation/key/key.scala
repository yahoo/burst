/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.cache.generation

import org.burstsys.agent.api.{BurstQueryApiGenerationKey, BurstQueryCacheGenerationKey}
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey

import scala.language.implicitConversions

package object key {

  implicit def thriftToFabricGenerationKey(a: BurstQueryApiGenerationKey): FabricGenerationKey =
    FabricGenerationKey(
      domainKey = a.domainKey,
      viewKey = a.viewKey,
      generationClock = a.generationClock
    )

  implicit def fabricToThriftGenerationKey(a: FabricGenerationKey): BurstQueryApiGenerationKey =
    BurstQueryApiGenerationKey(
      domainKey = a.domainKey,
      viewKey = a.viewKey,
      generationClock = a.generationClock
    )

  implicit def thriftToFabricCacheGenerationKey(a: BurstQueryCacheGenerationKey): FabricGenerationKey =
    FabricGenerationKey(
      domainKey = a.domainKey.getOrElse(-1),
      viewKey = a.viewKey.getOrElse(-1),
      generationClock = a.generationClock.getOrElse(-1)
    )

  implicit def fabricCacheGenerationKeyToThrift(a: FabricGenerationKey): BurstQueryCacheGenerationKey =
    BurstQueryCacheGenerationKey(
      domainKey = if (a.domainKey == -1) None else Some(a.domainKey),
      viewKey = if (a.viewKey == -1) None else Some(a.viewKey),
      generationClock = if (a.generationClock == -1) None else Some(a.generationClock)
    )

}
