/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra

import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekSupervisor}

package object trek {

  final object HydraSupervisorSchemaLookup extends VitalsTrekMark("hydra_supervisor_schema_lookup",
    cluster = VitalsTrekCell,
    role = VitalsTrekSupervisor
  )

  final object HydraSupervisorParse extends VitalsTrekMark("hydra_supervisor_parse",
    cluster = VitalsTrekCell,
    role = VitalsTrekSupervisor
  )

}
