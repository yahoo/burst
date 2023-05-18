/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result

import org.burstsys.agent.api._
import org.burstsys.agent.model.execution.group.datum._
import org.burstsys.agent.model.execution.group.key._
import org.burstsys.agent.model.execution.result.cell._
import org.burstsys.agent.model.execution.result.group.metrics._
import org.burstsys.agent.model.execution.result.set._
import org.burstsys.agent.model.execution.result.set.metrics._
import org.burstsys.agent.model.execution.result.status._
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.result.group.{FabricResultGroup, FabricResultGroupMetrics}
import org.burstsys.fabric.wave.execution.model.result.set.{FabricResultSet, FabricResultSetIndex, FabricResultSetName, FabricResultSets}
import org.burstsys.fabric.wave.execution.model.result.status.FabricResultStatus

import scala.collection.Map
import scala.language.implicitConversions

package object group {
  type AgentResultGroupUid = String

  type AgentResultGroupName = String

  type AgentResultGroup = BurstQueryApiResultGroup.Proxy

  type AgentThriftResultGroup = BurstQueryApiResultGroup

  type FabricResultSetNameMap = Map[FabricResultSetName, FabricResultSetIndex]

  implicit def nameMap(resultSets: AgentResultSetMap): collection.Map[FabricResultSetName, FabricResultSetIndex] =
    resultSets.map {
      r => r._2.resultName -> r._2.resultIndex
    }

  implicit def optionAgentResultSetToFabric(rs: Map[Int, BurstQueryApiResultSet]): FabricResultSets = {
    rs.map {
      case (k, v) => k -> (v: FabricResultSet)
    }.toMap
  }

  implicit
  def thriftToFabricResultGroup(a: AgentThriftResultGroup): FabricResultGroup = {
    FabricResultGroup(
      groupKey = a.groupKey: FabricGroupKey,
      resultStatus = a.resultStatus: FabricResultStatus,
      resultMessage = a.resultMessage,
      groupMetrics = a.groupMetrics: FabricResultGroupMetrics,
      resultSets = a.resultSets: FabricResultSets,
      rowCount = a.resultSets.foldRight(0)(_._2.rowCount + _)
    )
  }

  implicit
  def thriftToAgentResultGroup(a: AgentThriftResultGroup): AgentResultGroup =
    AgentResultGroupContext(a)

  implicit
  def fabricToAgentResultSetMap(resultGroup: FabricResultGroup): AgentResultSetMap = {
    resultGroup.resultSets.map {
      case (index, resultSet) =>
        index -> AgentResultSet(
          resultIndex = resultSet.resultIndex,
          resultName = resultSet.resultName,
          metrics = resultSet.metrics,
          columnNames = resultSet.columnNames.toSeq,
          columnTypes = resultSet.columnTypeKeys.map(datatypeFor).toIndexedSeq,
          rowSet = resultSet.rowSet.map(_.cells: Seq[AgentResultCell]).toSeq
        )
    }
  }

  implicit
  def fabricToAgentResultGroup(resultGroup: FabricResultGroup): AgentResultGroup = {
    BurstQueryApiResultGroup(
      groupKey = resultGroup.groupKey: BurstQueryApiGroupKey,
      resultStatus = resultGroup.resultStatus: BurstQueryApiResultStatus,
      resultMessage = resultGroup.resultMessage,
      groupMetrics = resultGroup.groupMetrics: BurstQueryApiResultGroupMetrics,
      nameMap = nameMap(resultGroup),
      resultSets = resultGroup
    )
  }


  final case
  class AgentResultGroupContext(_underlying_BurstQueryApiResultGroup: BurstQueryApiResultGroup)
    extends BurstQueryApiResultGroup.Proxy


}
