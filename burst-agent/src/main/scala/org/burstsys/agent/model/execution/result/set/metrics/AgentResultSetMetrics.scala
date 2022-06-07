/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result.set.metrics

import org.burstsys.agent.api.BurstQueryApiResultSetMetrics

import scala.language.implicitConversions

/**
 * constructors
 */
object AgentResultSetMetrics {


  def apply(
             succeeded: Boolean = false,
             rowCount: Long = 0L,
             limited: Boolean = false,
             overflowed: Boolean = false,
             properties: Map[String, String] = Map.empty
           ): AgentResultSetMetrics =
    BurstQueryApiResultSetMetrics(
      succeeded = succeeded,
      limited = limited,
      overflowed = overflowed,
      rowCount = rowCount,
      properties = properties
    )


}

private final case
class AgentResultSetMetricsContext(_underlying_BurstQueryApiResultSetMetrics: BurstQueryApiResultSetMetrics)
  extends AgentResultSetMetrics

