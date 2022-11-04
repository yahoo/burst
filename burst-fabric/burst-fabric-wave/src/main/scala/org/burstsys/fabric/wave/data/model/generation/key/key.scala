/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.generation

import org.burstsys.fabric.wave.metadata.model.FabricDomainKey
import org.burstsys.fabric.wave.metadata.model.FabricGenerationClock
import org.burstsys.fabric.wave.metadata.model.FabricViewKey
import org.burstsys.vitals.json.VitalsJsonObject

package object key {

  final case class JsonFabricGenerationKey(domainKey: FabricDomainKey,
                                           viewKey: FabricViewKey,
                                           generationClock: FabricGenerationClock)
    extends FabricGenerationKey with VitalsJsonObject {

    override def init(gm: FabricGenerationIdentity): FabricGenerationKey = jsonMethodException

    override def init(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricGenerationKey = jsonMethodException
  }

}
