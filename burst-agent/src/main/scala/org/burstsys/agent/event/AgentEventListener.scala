/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.event

import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.result.status.FabricResultStatus
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.vitals.uid.VitalsUid

/**
 * The Agent request execution lifecycle pipeline events
 */
trait AgentEventListener extends Any {

  /**
   * a request has begun
   */
  def onAgentRequestBegin(guid: VitalsUid, source: String, over: FabricOver, call: Option[FabricCall]): Unit = {}

  /**
   * a request succeeded
   */
  def onAgentRequestSucceed(guid: VitalsUid): Unit = {}

  /**
   * a request failed
   */
  def onAgentRequestFail(guid: VitalsUid, status: FabricResultStatus, msg: String): Unit = {}

}
