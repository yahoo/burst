/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result

import org.burstsys.agent.api.BurstQueryApiResultStatus
import org.burstsys.agent.api.BurstQueryApiResultStatus._
import org.burstsys.fabric.execution.model.result.status._

import scala.language.implicitConversions

package object status {

  type AgentResultStatus = BurstQueryApiResultStatus

  final val BurstQueryInProgressStatus: AgentResultStatus = BurstQueryApiInProgressStatus
  final val BurstQueryUnknownStatus: AgentResultStatus = BurstQueryApiUnknownStatus
  final val BurstQuerySuccessStatus: AgentResultStatus = BurstQueryApiSuccessStatus
  final val BurstQueryExceptionStatus: AgentResultStatus = BurstQueryApiExceptionStatus
  final val BurstQueryInvalidStatus: AgentResultStatus = BurstQueryApiInvalidStatus
  final val BurstQueryTimeoutStatus: AgentResultStatus = BurstQueryApiTimeoutStatus
  final val BurstQueryNotReadyStatus: AgentResultStatus = BurstQueryApiNotReadyStatus
  final val BurstQueryNoDataStatus: AgentResultStatus = BurstQueryApiNoDataStatus
  final val BurstQueryStoreErrorStatus: AgentResultStatus = BurstQueryApiNoDataStatus

  /**
   * convert fabric status to thrift/agent status
   *
   * @param fabric
   * @return
   */
  implicit def fabricToAgentResultStatus(fabric: FabricResultStatus): AgentResultStatus = fabric match {
    case FabricInProgressResultStatus => BurstQueryInProgressStatus
    case FabricUnknownResultStatus => BurstQueryUnknownStatus
    case FabricSuccessResultStatus => BurstQuerySuccessStatus
    case FabricFaultResultStatus => BurstQueryExceptionStatus
    case FabricInvalidResultStatus => BurstQueryInvalidStatus
    case FabricTimeoutResultStatus => BurstQueryTimeoutStatus
    case FabricNotReadyResultStatus => BurstQueryNotReadyStatus
    case FabricNoDataResultStatus => BurstQueryNoDataStatus
    case FabricStoreErrorResultStatus => BurstQueryStoreErrorStatus
    case _ => ???
  }

  /**
   * convert thrift/agent status to fabric status
   *
   * @param agent
   * @return
   */
  implicit def agentToFabricResultStatus(agent: AgentResultStatus): FabricResultStatus = agent match {
    case BurstQueryInProgressStatus => FabricInProgressResultStatus
    case BurstQueryUnknownStatus => FabricUnknownResultStatus
    case BurstQuerySuccessStatus => FabricSuccessResultStatus
    case BurstQueryExceptionStatus => FabricFaultResultStatus
    case BurstQueryInvalidStatus => FabricInvalidResultStatus
    case BurstQueryTimeoutStatus => FabricTimeoutResultStatus
    case BurstQueryNotReadyStatus => FabricNotReadyResultStatus
    case BurstQueryNoDataStatus => FabricNoDataResultStatus
    case BurstQueryStoreErrorStatus => FabricStoreErrorResultStatus
    case _ => ???
  }

}
