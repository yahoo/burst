/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.api.server

import org.burstsys.agent.AgentService
import org.burstsys.agent.api._
import org.burstsys.api.BurstApiServer
import org.burstsys.vitals.VitalsService.VitalsServiceModality

private[agent] final case
class AgentApiServer(service: AgentService, modality: VitalsServiceModality) extends BurstApiServer
  with AgentApi with AgentQueryReactor with AgentCacheReactor {

}
