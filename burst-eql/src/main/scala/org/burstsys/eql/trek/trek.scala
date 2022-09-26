/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql

import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekSupervisor}

package object trek {

  final object EqlSupervisorQueryParse extends VitalsTrekMark("eql_supervisor_query_parse",
    cluster = VitalsTrekCell,
    role = VitalsTrekSupervisor
  )

  final object EqlSupervisorQueryPlan extends VitalsTrekMark("eql_supervisor_query_plan",
    cluster = VitalsTrekCell,
    role = VitalsTrekSupervisor
  )

  final object EqlSupervisorQueryGenerate extends VitalsTrekMark("eql_supervisor_query_generate",
    cluster = VitalsTrekCell,
    role = VitalsTrekSupervisor
  )

}
