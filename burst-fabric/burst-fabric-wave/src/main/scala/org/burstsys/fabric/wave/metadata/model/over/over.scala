/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.metadata.model

import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.time.VitalsLocale

package object over {

  final case class JsonFabricOver(domainKey: FabricDomainKey, viewKey: FabricViewKey, locale: VitalsLocale)
    extends FabricOver with VitalsJsonObject {

    override def domainKey_=(l: FabricDomainKey): Unit = jsonMethodException

    override def viewKey_=(l: FabricViewKey): Unit = jsonMethodException

    override def locale_=(l: VitalsLocale): Unit = jsonMethodException
  }

}
