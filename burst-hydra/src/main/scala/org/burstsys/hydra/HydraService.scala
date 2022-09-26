/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra

import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.execution.supervisor.group.FabricGroupExecuteContext
import org.burstsys.fabric.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.gather.data.FabricDataGather
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.felt.FeltService
import org.burstsys.felt.model.collectors.result.FeltCollectorResultGroup
import org.burstsys.felt.model.collectors.runtime.FeltCollectorGather
import org.burstsys.hydra.execute.HydraWaveExecutor
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsPojo

import scala.concurrent.Future

/**
 * the supervisor side hydra pipeline front end
 */
trait HydraService extends FabricSupervisorService {

  /**
   * execute a hydra analysis in hydra source language
   *
   * @param guid
   * @param source
   * @param over
   * @return
   */
  def executeHydraAsWave(
                          guid: FabricGroupUid,
                          source: String,
                          over: FabricOver,
                          parameters: Option[FabricCall] = None
                        ): Future[FabricResultGroup]

}

object HydraService {

  def apply(container: FabricSupervisorContainer): HydraService = HydraServiceContext(container: FabricSupervisorContainer)

}

final case
class HydraServiceContext(container: FabricSupervisorContainer) extends FabricGroupExecuteContext with HydraWaveExecutor {

  override def modality: VitalsService.VitalsServiceModality = VitalsPojo

  override val serviceName: String = s"hydra"

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override protected
  def extractResults(gather: FabricDataGather): FabricResultGroup = {
    gather match {
      case g: FeltCollectorGather =>
        FeltCollectorResultGroup(gather.groupKey, g).extractResults
      case _ =>
        ???
    }

  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    synchronized {
      ensureNotRunning
      log info startingMessage
      FeltService.start
      markRunning
    }
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      log info stoppingMessage
      FeltService.stop
      markNotRunning
    }
  }

}
