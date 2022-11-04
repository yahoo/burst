/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.processors

import org.burstsys.agent.{AgentLanguage, AgentService}
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.hydra.HydraService
import org.burstsys.tesla.thread.request._

import scala.concurrent.Future

/**
 * process and execute Hydra language analysis
 * <hr/> '''NOTE:''' this is designed to be imported into Supervisor Process as well<hr/>
 */
final case
class BurstSystemHydraQueryProcessor(agent: AgentService, hydra: HydraService) extends AgentLanguage {

  override val languagePrefixes: Array[String] = Array("hydra")

  override
  def executeGroupAsWave(groupUid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall]): Future[FabricExecuteResult] = {
    hydra.executeHydraAsWave(groupUid, source, over, call) map (FabricExecuteResult(_))
  }

}
