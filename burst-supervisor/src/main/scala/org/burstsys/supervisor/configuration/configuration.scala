/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor

import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.properties.VitalsPropertySpecification

import scala.concurrent.duration.Duration

package object configuration extends VitalsPropertyRegistry {

  val burstSupervisorPropertiesFileProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.supervisor.properties.file",
    description = "Location of properties file containing environment specific variables",
    default = Some("burst-supervisor-local.properties")
  )

  val kedaScaleDownInterval: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = "burst.supervisor.keda.scale.down.interval",
    description = "Interval between scale down events",
    default = Some(Duration(5, "minutes"))
  )

  val kedaScaleUpWorkerCount: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.supervisor.keda.scale.up.worker.count",
    description = "Number of workers to scale up to",
    default = Some(20)
  )
}
