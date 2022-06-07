/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.metrics

import org.burstsys.agent.api.BurstQueryApiExecutionMetrics

import scala.language.implicitConversions

object AgentExecutionMetrics {

  def apply(
             scanTime: Long = 0L,
             scanWork: Long = 0L,
             queryCount: Long = 0L,
             rowCount: Long = 0L,
             succeeded: Long = 0L,
             limited: Long = 0L,
             overflowed: Long = 0L,
             compileTime: Long = 0L,
             cacheHits: Long = 0L
           ): AgentThriftExecutionMetrics =
    BurstQueryApiExecutionMetrics(
      scanTime = scanTime,
      scanWork = scanWork,
      queryCount = queryCount,
      rowCount = rowCount,
      succeeded = succeeded,
      limited = limited,
      overflowed = overflowed,
      compileTime = compileTime,
      cacheHits = cacheHits
    )

}

private final case
class AgentExecutionMetricsContext(_underlying_BurstQueryApiExecutionMetrics: BurstQueryApiExecutionMetrics)
  extends BurstQueryApiExecutionMetrics.Proxy {
}

