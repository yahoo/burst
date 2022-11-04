/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution

import org.burstsys.agent.api.BurstQueryApiExecutionMetrics
import org.burstsys.fabric.wave.execution.model.metrics.FabricExecutionMetrics

import scala.language.implicitConversions

package object metrics {

  type AgentExecutionMetrics = BurstQueryApiExecutionMetrics.Proxy

  type AgentThriftExecutionMetrics = BurstQueryApiExecutionMetrics

  implicit def fabricToThriftExecutionMetrics(em: FabricExecutionMetrics): AgentThriftExecutionMetrics = {
    BurstQueryApiExecutionMetrics(
      scanTime = em.scanTime,
      scanWork = em.scanWork,
      queryCount = em.queryCount,
      rowCount = em.rowCount,
      succeeded = em.succeeded,
      limited = em.limited,
      overflowed = em.overflowed,
      compileTime = em.compileTime,
      cacheHits = em.cacheHits
    )
  }

  implicit def thriftToFabricExecutionMetrics(em: AgentThriftExecutionMetrics): FabricExecutionMetrics = {
    FabricExecutionMetrics().init(
      scanTime = em.scanTime,
      scanWork = em.scanWork,
      scanTimeSkew = 0, // TODO implement in thrift API
      scanWorkSkew = 0, // TODO implement in thrift API
      queryCount = em.queryCount,
      rowCount = em.rowCount,
      succeeded = em.succeeded,
      limited = em.limited,
      overflowed = em.overflowed,
      compileTime = em.compileTime,
      cacheHits = em.cacheHits
    )
  }
}
