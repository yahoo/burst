/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.group

import org.burstsys.agent.api.BurstQueryApiOver
import org.burstsys.fabric.metadata.model.over.FabricOver

import scala.language.implicitConversions

package object over {

  type AgentOver = BurstQueryApiOver.Proxy

  type AgentThriftOver = BurstQueryApiOver

  final case
  class AgentOverContext(_underlying_BurstQueryApiOver: BurstQueryApiOver) extends AgentOver

  implicit def thriftToAgentOver(a: BurstQueryApiOver): AgentOver = AgentOverContext(a)

  implicit def fabricToAgentOver(over: FabricOver): AgentOver =
    BurstQueryApiOver(
      domainKey = over.domainKey,
      viewKey = over.viewKey,
      timeZone = Some(over.locale.timezone)
    )

  implicit def thriftToFabricOver(over: AgentThriftOver): FabricOver =
    FabricOver(
      domain = over.domainKey,
      view = over.viewKey,
      locale = over.timeZone
    )

}
