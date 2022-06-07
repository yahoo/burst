/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra

import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekMaster}

package object trek {

  final object HydraMasterSchemaLookup extends VitalsTrekMark("hydra_master_schema_lookup",
    cluster = VitalsTrekCell,
    role = VitalsTrekMaster
  )

  final object HydraMasterParse extends VitalsTrekMark("hydra_master_parse",
    cluster = VitalsTrekCell,
    role = VitalsTrekMaster
  )

}
