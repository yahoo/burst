/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekClient, VitalsTrekMark, VitalsTrekServer, VitalsTrekSupervisor}

package object trek {

  final object FabricNetHeartbeat extends VitalsTrekMark("fabric_net_heartbeat",
    cluster = VitalsTrekCell,
    role = VitalsTrekClient
  )

  final object FabricNetAssessResp extends VitalsTrekMark("fabric_net_assess_resp",
    cluster = VitalsTrekCell,
    role = VitalsTrekClient
  )

  final object FabricNetAssessReq extends VitalsTrekMark("fabric_net_assess_req",
    cluster = VitalsTrekCell,
    role = VitalsTrekServer
  )
}
