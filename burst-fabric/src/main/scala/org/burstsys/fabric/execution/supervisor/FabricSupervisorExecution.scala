/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.supervisor

import org.burstsys.fabric.container.FabricSupervisorService
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.execution.supervisor.wave.{FabricWaveListener, FabricWaveLoop, FabricWaveScatter, FabricWaveTalker}
import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.fabric.execution.model.wave.FabricWave
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.vitals.VitalsService.VitalsServiceModality

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * '''SUPERVISOR''' side control of distributed cell execution
 */
trait FabricSupervisorExecution extends FabricSupervisorService with FabricWaveListener {

  /**
   * execute a scatter/gather wave operation )
   * All errors are thrown as a [[org.burstsys.fabric.exception.FabricException]]
   *
   * @return
   */
  def executionWaveOp(wave: FabricWave): Future[FabricGather]

}

object FabricSupervisorExecution {

  def apply(container: FabricSupervisorContainer): FabricSupervisorExecution =
    FabricSupervisorExecutionContext(container: FabricSupervisorContainer)

}

private[fabric] final case
class FabricSupervisorExecutionContext(container: FabricSupervisorContainer)
  extends FabricSupervisorExecution with FabricNetServerListener with FabricWaveScatter
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
    container.netServer talksTo this
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

