/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result.set

import org.burstsys.agent.api.{BurstQueryApiResultSet, BurstQueryDataType}
import org.burstsys.agent.model.execution.result.cell.AgentResultCell
import org.burstsys.agent.model.execution.result.set.metrics.AgentResultSetMetrics
import org.burstsys.fabric.execution.model.result.set.{FabricResultSetIndex, FabricResultSetName}

import scala.language.implicitConversions

object AgentResultSet {

  def apply(
             resultIndex: FabricResultSetIndex,
             resultName: FabricResultSetName,
             metrics: AgentResultSetMetrics,
             columnNames: Seq[String],
             columnTypes: Seq[BurstQueryDataType],
             rowSet: Seq[Seq[AgentResultCell]]
           ): AgentResultSet = {
    BurstQueryApiResultSet(
      resultIndex = resultIndex,
      resultName = resultName,
      metrics = metrics,
      columnNames = columnNames,
      columnTypes = columnTypes,
      rowSet = rowSet
    )
  }


}

final case
class AgentResultSetContext(_underlying_BurstQueryApiResultSet: AgentThriftResultSet)
  extends BurstQueryApiResultSet.Proxy

