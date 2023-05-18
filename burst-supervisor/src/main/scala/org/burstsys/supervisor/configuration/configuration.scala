/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor

import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.properties.VitalsPropertySpecification

package object configuration extends VitalsPropertyRegistry {

  val burstSupervisorPropertiesFileProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.supervisor.properties.file",
    description = "Location of properties file containing environment specific variables",
    default = Some("burst-supervisor-local.properties")
  )
}
