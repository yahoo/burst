/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.supervisor

import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.wave.container.supervisor.{FabricWaveSupervisorContainer, FabricWaveSupervisorListener}
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.wave.FabricWave
import org.burstsys.fabric.wave.execution.supervisor.wave.{FabricWaveListener, FabricWaveLoop, FabricWaveScatter, FabricWaveTalker}
import org.burstsys.vitals.VitalsService.VitalsServiceModality

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * '''SUPERVISOR''' side control of distributed cell execution
 */
trait FabricSupervisorExecution extends FabricSupervisorService with FabricWaveListener {

  /**
   * execute a scatter/gather wave operation )
   * All errors are thrown as a [[org.burstsys.fabric.wave.exception.FabricException]]
   *
   * @return
   */
  def executionWaveOp(wave: FabricWave): Future[FabricGather]

}

object FabricSupervisorExecution {

  def apply(container: FabricWaveSupervisorContainer): FabricSupervisorExecution =
    FabricWaveSupervisorExecutionContext(container: FabricWaveSupervisorContainer)

}

private[fabric] final case
class FabricWaveSupervisorExecutionContext(container: FabricWaveSupervisorContainer)
  extends FabricSupervisorExecution with FabricWaveSupervisorListener with FabricWaveScatter
    with FabricWaveTalker with FabricWaveLoop {

  override def serviceName: String = s"fabric-supervisor-execution"

  override def modality: VitalsServiceModality = container.bootModality

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    container talksTo this
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    markNotRunning
    this
  }

}

