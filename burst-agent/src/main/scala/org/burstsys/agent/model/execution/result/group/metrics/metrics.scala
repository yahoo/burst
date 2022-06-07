/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result.group

import org.burstsys.agent.api.{BurstQueryApiLoadMode, BurstQueryApiResultGroupMetrics}
import org.burstsys.agent.model.cache.generation.key._
import org.burstsys.agent.model.cache.generation.metrics._
import org.burstsys.agent.model.execution.group.key._
import org.burstsys.agent.model.execution.metrics._
import org.burstsys.agent.model.execution.result.status._
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.data.model.mode._
import org.burstsys.fabric.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.execution.model.metrics.FabricExecutionMetrics
import org.burstsys.fabric.execution.model.result.group.FabricResultGroupMetrics
import org.burstsys.fabric.execution.model.result.status._
import org.burstsys.fabric.execution.model.result.group

import scala.language.implicitConversions

package object metrics {

  type AgentResultGroupMetrics = BurstQueryApiResultGroupMetrics.Proxy

  type AgentThriftResultGroupMetrics = BurstQueryApiResultGroupMetrics

  implicit def fabricToThriftLoadType(lm: FabricLoadMode): BurstQueryApiLoadMode =
    lm match {
      case FabricUnknownLoad => BurstQueryApiLoadMode.UnknownLoad
      case FabricNoDataLoad => BurstQueryApiLoadMode.NoDataLoad
      case FabricErrorLoad => BurstQueryApiLoadMode.ErrorLoad
      case FabricColdLoad => BurstQueryApiLoadMode.ColdLoad
      case FabricWarmLoad => BurstQueryApiLoadMode.WarmLoad
      case FabricHotLoad => BurstQueryApiLoadMode.HotLoad
      case _ => ???
    }

  implicit def thriftToFabricLoadType(lm: BurstQueryApiLoadMode): FabricLoadMode =
    lm match {
      case BurstQueryApiLoadMode.UnknownLoad => FabricUnknownLoad
      case BurstQueryApiLoadMode.NoDataLoad => FabricNoDataLoad
      case BurstQueryApiLoadMode.ErrorLoad => FabricErrorLoad
      case BurstQueryApiLoadMode.ColdLoad => FabricColdLoad
      case BurstQueryApiLoadMode.WarmLoad => FabricWarmLoad
      case BurstQueryApiLoadMode.HotLoad => FabricHotLoad
      case _ => ???
    }

  implicit def thriftToAgentResultGroupMetrics(a: AgentThriftResultGroupMetrics): AgentResultGroupMetrics =
    AgentResultGroupMetricsContext(a)

  implicit def fabricToAgentResultGroupMetrics(rg: FabricResultGroupMetrics): AgentResultGroupMetrics = {
    AgentResultGroupMetrics(
      groupKey = rg.groupKey,
      generationKey = rg.generationKey,
      generationMetrics = rg.generationMetrics,
      executionMetrics = rg.executionMetrics,
      resultStatus = rg.resultStatus,
      resultMessage = rg.resultMessage
    )
  }

  implicit def thriftToFabricResultGroupMetrics(rg: AgentThriftResultGroupMetrics): FabricResultGroupMetrics = {
    FabricResultGroupMetrics(
      groupKey = rg.groupKey: FabricGroupKey,
      generationKey = rg.generationKey: FabricGenerationKey,
      generationMetrics = rg.generationMetrics: FabricGenerationMetrics,
      executionMetrics = rg.executionMetrics: FabricExecutionMetrics,
      resultStatus = rg.resultStatus: FabricResultStatus,
      resultMessage = rg.resultMessage: String
    )
  }

}
