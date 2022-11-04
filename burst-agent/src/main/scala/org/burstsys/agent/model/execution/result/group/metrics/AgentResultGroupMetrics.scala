/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result.group.metrics

import org.burstsys.agent.api._
import org.burstsys.agent.model.cache.generation.key._
import org.burstsys.agent.model.cache.generation.metrics._
import org.burstsys.agent.model.execution.group.key._
import org.burstsys.agent.model.execution.metrics._
import org.burstsys.agent.model.execution.result.status._
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.metrics.FabricExecutionMetrics
import org.burstsys.fabric.wave.execution.model.result.status._

import scala.language.implicitConversions

object AgentResultGroupMetrics {

  def apply(
             groupKey: FabricGroupKey,
             resultStatus: FabricResultStatus,
             resultMessage: String,
             generationKey: FabricGenerationKey,
             generationMetrics: FabricGenerationMetrics,
             executionMetrics: FabricExecutionMetrics
           ): AgentResultGroupMetrics = {
    BurstQueryApiResultGroupMetrics(
      groupKey = groupKey: BurstQueryApiGroupKey,
      generationKey = generationKey: BurstQueryApiGenerationKey,
      resultStatus = resultStatus: BurstQueryApiResultStatus,
      resultMessage = resultMessage,
      generationMetrics = generationMetrics: BurstQueryCacheGenerationMetrics,
      executionMetrics = executionMetrics: BurstQueryApiExecutionMetrics
    )

  }


}

private final case
class AgentResultGroupMetricsContext(_underlying_BurstQueryApiResultGroupMetrics: BurstQueryApiResultGroupMetrics)
  extends BurstQueryApiResultGroupMetrics.Proxy {
}

