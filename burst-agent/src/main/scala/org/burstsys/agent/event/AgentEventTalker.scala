/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.event

import io.opentelemetry.api.trace.Span
import org.burstsys.agent.AgentServiceContext
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.pipeline.publishPipelineEvent
import org.burstsys.fabric.wave.execution.model.result.status.FabricResultStatus
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.vitals.uid._

/**
 * Bridge between Agent request execution lifecycle events and the generic
 * Fabric pipeline event model
 */
trait AgentEventTalker extends AnyRef with AgentEventListener {

  self: AgentServiceContext =>

  private[this]
  def publishAgentEvent(event: AgentRequestEvent): Unit = publishPipelineEvent(event)

  final override
  def onAgentRequestBegin(guid: VitalsUid, source: String, over: FabricOver, call: Option[FabricCall]): Unit = {
    val traceId = Span.current.getSpanContext.getTraceId
    publishAgentEvent(AgentRequestStarted(guid, traceId, source, over, call))
  }

  final override
  def onAgentRequestSucceed(guid: VitalsUid): Unit = {
    publishAgentEvent(AgentRequestSucceeded(guid))
  }

  final override
  def onAgentRequestFail(guid: VitalsUid, status: FabricResultStatus, msg: String): Unit = {
    publishAgentEvent(AgentRequestFailed(guid, status, msg))
  }

}

