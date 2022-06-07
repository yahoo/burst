/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent

import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.pipeline.FabricPipelineEvent
import org.burstsys.fabric.execution.model.result.status.FabricResultStatus
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.vitals.uid.VitalsUid

package object event {

  /**
   * Base type for a series of Agent pipeline execution lifecycle 'events'
   * based on the Fabric layer pipeline event model
   */
  trait AgentRequestEvent extends FabricPipelineEvent

  /**
   * the start of the Agent request execution lifecycle
   * @param guid
   * @param source
   * @param over
   */
  final case class AgentRequestStarted(guid: VitalsUid, source: String, over: FabricOver, call: Option[FabricCall]) extends AgentRequestEvent

  /**
   * the success completion of the Agent request execution lifecycle
   * @param guid
   */
  final case class AgentRequestSucceeded(guid: VitalsUid) extends AgentRequestEvent

  /**
   * the failure completion of the Agent request execution lifecycle
   *
   * @param guid
   * @param message
   */
  final case class AgentRequestFailed(guid: VitalsUid, status: FabricResultStatus, message: String) extends AgentRequestEvent

}
