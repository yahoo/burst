/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.metadata.model

import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.fabric.data.model.generation.FabricGenerationIdentity
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.properties.VitalsPropertyMap

package object view {

  final case class JsonFabricView(
                                   schemaName: BrioSchemaName,
                                   storeProperties: VitalsPropertyMap,
                                   viewMotif: String,
                                   viewProperties: VitalsPropertyMap,
                                   domainKey: FabricDomainKey,
                                   viewKey: FabricViewKey,
                                   generationClock: FabricGenerationClock
                                 )
    extends FabricView with VitalsJsonObject {

    override def init(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricView = jsonMethodException

    override def init(gm: FabricGenerationIdentity): FabricView = jsonMethodException
  }

}
