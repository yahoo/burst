/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result.cell

import org.burstsys.agent.api.BurstQueryApiCellType
import org.burstsys.agent.api.BurstQueryApiCellType.{AggregationCell, DimensionCell}
import org.burstsys.fabric.execution.model.result.row.{FabricAggregationCell, FabricDimensionCell, FabricResultCellType}

import scala.language.implicitConversions

package object tipe {

  type AgentCellType = BurstQueryApiCellType

  final val BurstQueryApiAggregationCell: AgentCellType = AggregationCell

  final val BurstQueryApiDimensionCell: AgentCellType = DimensionCell

  /**
   * convert fabric cell type to thrift/agent status
   *
   * @param fabric
   * @return
   */
  implicit def fabricToAgentCellType(fabric: FabricResultCellType): AgentCellType = fabric match {
    case FabricAggregationCell => BurstQueryApiAggregationCell

    case FabricDimensionCell => BurstQueryApiDimensionCell

    case _ => ???
  }

  /**
   * convert thrift/agent cell type to fabric status
   *
   * @param agent
   * @return
   */
  implicit def agentToFabricCellType(agent: AgentCellType): FabricResultCellType = agent match {
    case BurstQueryApiAggregationCell => FabricAggregationCell

    case BurstQueryApiDimensionCell => FabricDimensionCell

    case _ => ???
  }

}
