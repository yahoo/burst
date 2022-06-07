/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql

import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekMaster}

package object trek {

  final object EqlMasterQueryParse extends VitalsTrekMark("eql_master_query_parse",
    cluster = VitalsTrekCell,
    role = VitalsTrekMaster
  )

  final object EqlMasterQueryPlan extends VitalsTrekMark("eql_master_query_plan",
    cluster = VitalsTrekCell,
    role = VitalsTrekMaster
  )

  final object EqlMasterQueryGenerate extends VitalsTrekMark("eql_master_query_generate",
    cluster = VitalsTrekCell,
    role = VitalsTrekMaster
  )

}
