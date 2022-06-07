/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.metadata.model

import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.properties.VitalsPropertyMap

package object domain {

  final case class JsonFabricDomain(domainKey: FabricDomainKey, domainProperties: VitalsPropertyMap)
    extends FabricDomain with VitalsJsonObject

}
