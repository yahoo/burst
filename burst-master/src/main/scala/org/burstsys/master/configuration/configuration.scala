/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master

import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.properties.VitalsPropertySpecification

package object configuration extends VitalsPropertyRegistry {

  val burstMasterPropertiesFileProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.master.properties.file",
    description = "Location of properties file containing environment specific variables",
    default = Some("burst-master-local.properties")
  )

  val burstMasterJsonWatchDirectoryProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.master.jsonfilemanager.watchdirectory",
    description = "Location of directory the JSON file manager will monitor [default=empty and manager is disabled]",
    default = None
  )

}
