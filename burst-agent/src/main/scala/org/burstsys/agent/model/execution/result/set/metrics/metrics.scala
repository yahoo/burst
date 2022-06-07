/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result.set

import org.burstsys.agent.api.BurstQueryApiResultSetMetrics
import org.burstsys.fabric.execution.model.result.set.FabricResultSetMetrics

import scala.language.implicitConversions

package object metrics {

  type AgentResultSetMetrics = BurstQueryApiResultSetMetrics.Proxy

  type AgentThriftResultSetMetrics = BurstQueryApiResultSetMetrics

  implicit def thriftToAgentResultSetMetrics(a: AgentThriftResultSetMetrics): AgentResultSetMetrics =
    AgentResultSetMetricsContext(a)

  implicit def agentToThriftResultSetMetrics(a: AgentResultSetMetrics): AgentThriftResultSetMetrics =
    BurstQueryApiResultSetMetrics(
      succeeded = a.succeeded,
      limited = a.limited,
      overflowed = a.overflowed,
      rowCount = a.rowCount,
      properties = a.properties
    )

  implicit def thriftToFabricResultSetMetrics(rs: BurstQueryApiResultSetMetrics): FabricResultSetMetrics =
    FabricResultSetMetrics(
      succeeded = rs.succeeded,
      rowCount = rs.rowCount,
      limited = rs.limited,
      overflowed = rs.overflowed,
      properties = rs.properties.toMap
    )

  implicit def fabricToAgentResultSetMetrics(rs: FabricResultSetMetrics): AgentResultSetMetrics =
    AgentResultSetMetrics(
      succeeded = rs.succeeded,
      rowCount = rs.rowCount,
      limited = rs.limited,
      overflowed = rs.overflowed,
      properties = rs.properties
    )
}
