/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result

import org.burstsys.agent.api.BurstQueryApiResultSet
import org.burstsys.agent.model.execution.group.datum._
import org.burstsys.agent.model.execution.result.row._
import org.burstsys.agent.model.execution.result.set.metrics._
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.fabric.execution.model.result.set.{FabricResultSet, FabricResultSetIndex}

import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.collection.Map
import scala.language.implicitConversions

package object set {

  type AgentResultSet = BurstQueryApiResultSet.Proxy

  type AgentThriftResultSet = BurstQueryApiResultSet

  type AgentResultSetMap = Map[FabricResultSetIndex, BurstQueryApiResultSet]

  implicit def thriftToAgentResultSet(resultSet: AgentThriftResultSet): AgentResultSet =
    AgentResultSetContext(resultSet)

  implicit def agentToThriftResultSet(resultSet: AgentResultSet): AgentThriftResultSet =
    BurstQueryApiResultSet(
      resultIndex = resultSet.resultIndex,
      resultName = resultSet.resultName,
      metrics = resultSet.metrics,
      columnNames = resultSet.columnNames,
      columnTypes = resultSet.columnTypes,
      rowSet = resultSet.rowSet
    )

  implicit def thriftToFabricResultSet(resultSet: AgentThriftResultSet): FabricResultSet = {
    FabricResultSet(
      resultIndex = resultSet.resultIndex,
      resultName = resultSet.resultName,
      metrics = resultSet.metrics,
      dimensionCount = 0,
      aggregationCount = 0,
      columnNames = resultSet.columnNames.toArray,
      columnTypeNames = resultSet.columnTypes.map(_.name).toArray,
      columnTypeKeys = resultSet.columnTypes.map(datatypeOf).toArray,
      rowCount = resultSet.rowSet.length,
      rowSet = resultSet.rowSet.map(rs=>thriftToFabricResultRow(rs.toSeq)).toArray
    )
  }

}
