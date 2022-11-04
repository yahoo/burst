/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution

import org.burstsys.agent.api.{BurstQueryApiExecuteResult, BurstQueryApiResultGroup, BurstQueryApiResultStatus}
import org.burstsys.agent.model.execution.result.group._
import org.burstsys.agent.model.execution.result.status._
import org.burstsys.fabric.wave.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup

import scala.language.implicitConversions

package object result {

  type AgentExecuteResult = BurstQueryApiExecuteResult.Proxy

  type ThriftAgentExecuteResult = BurstQueryApiExecuteResult

  implicit def thriftToAgentExecuteResult(a: BurstQueryApiExecuteResult): AgentExecuteResult = AgentExecuteResultContext(a)

  implicit def fabricToAgentExecuteResult(a: FabricExecuteResult): AgentExecuteResult =
    BurstQueryApiExecuteResult(
      resultStatus = a.resultStatus: BurstQueryApiResultStatus,
      resultMessage = a.resultMessage,
      resultGroup = a.resultGroup match {
        case None => None
        case Some(rg) => Some(rg: BurstQueryApiResultGroup)
      }
    )

  implicit def thriftToFabricExecuteResult(a: ThriftAgentExecuteResult): FabricExecuteResult =
    FabricExecuteResult(
      resultStatus = a.resultStatus,
      resultMessage = a.resultMessage,
      resultGroup = a.resultGroup match {
        case None => None
        case Some(rg) => Some(rg: FabricResultGroup)
      }
    )

}
