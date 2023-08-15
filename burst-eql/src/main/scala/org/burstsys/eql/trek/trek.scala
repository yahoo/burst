/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql

import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekSupervisor}

package object trek {

  final object EqlSupervisorQueryParse extends VitalsTrekMark("EqlSupervisorQueryParse",
    VitalsTrekCell, VitalsTrekSupervisor
  )

  final object EqlSupervisorQueryPlan extends VitalsTrekMark("EqlSupervisorQueryPlan",
    VitalsTrekCell, VitalsTrekSupervisor
  )

  final object EqlSupervisorQueryGenerate extends VitalsTrekMark("EqlSupervisorQueryGenerate",
    VitalsTrekCell, VitalsTrekSupervisor
  )

}
