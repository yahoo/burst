/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.group

import org.burstsys.agent.api.{BurstQueryApiCall, BurstQueryApiParameter}
import org.burstsys.agent.model.execution.group.parameter._
import org.burstsys.fabric.wave.execution.model.execute.parameters.{FabricCall, FabricParameterValue}

import scala.language.implicitConversions

package object call {

  type AgentCall = BurstQueryApiCall.Proxy

  type AgentThriftCall = BurstQueryApiCall

  implicit def thriftToFabricCall(a: AgentThriftCall): FabricCall =
    FabricCall(a.parameters.map(p => p: FabricParameterValue).toArray)

  implicit def fabricToAgentCall(a: FabricCall): BurstQueryApiCall =
    BurstQueryApiCall(a.parameters.map(p => p: BurstQueryApiParameter))
}
