/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.model

import org.burstsys.fabric.container.FabricContainerId
import org.burstsys.tesla.part.factory.TeslaFactoryBoss
import org.burstsys.vitals.VitalsService.VitalsContainer
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.configuration.burstCellNameProperty
import org.burstsys.vitals.errors._
import org.burstsys.vitals.healthcheck.VitalsHealthCheckService
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.git
import org.burstsys.vitals.reporter

/**
 * A top level JVM singleton representing the Burst node process - supervisor or worker.
 * All of the functionality in common across both.
 */
trait FabricContainer extends VitalsService {

  /**
   * @return unique key for each container
   */
  def containerId: Option[FabricContainerId]

  final def containerIdGetOrThrow: FabricContainerId = containerId.getOrElse(throw VitalsException(s"no container id"))

  def containerId_=(id: FabricContainerId): Unit

  /**
   * The modality that this container had at boot
   */
  def bootModality: VitalsServiceModality

  /**
   * the health check service
   */
  def health: VitalsHealthCheckService

  /**
   * suspend main thread into background until container is finished
   */
  final def run: this.type = {
    Thread.currentThread.join()
    this
  }

  def log4JPropertiesFileName: String

}

abstract
class FabricContainerContext extends FabricContainer {

  final override val modality: VitalsServiceModality = VitalsContainer

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private[this]
  var _containerId: Option[FabricContainerId] = None

  final private[this]
  val _healthCheck: VitalsHealthCheckService = VitalsHealthCheckService(serviceName)

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override def containerId: Option[FabricContainerId] = _containerId

  final override def containerId_=(id: FabricContainerId): Unit = _containerId = Some(id)

  final override def health: VitalsHealthCheckService = _healthCheck

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    try {
      synchronized {
        ensureNotRunning
        _healthCheck.start

        VitalsLog.configureLogging(log4JPropertiesFileName)
        VitalsPropertyRegistry.logReport

        log info s"FABRIC_CELL_NAME: '${burstCellNameProperty.getOrThrow}'"
        log info s"FABRIC_GIT_BRANCH: '${git.branch}'   FABRIC_GIT_COMMIT: '${git.commitId}'"
        TeslaFactoryBoss.startIfNotAlreadyStarted

        reporter.startReporterSystem()

        markRunning
      }
      log info s"FABRIC_CONTAINER_BOOT containerId=$containerId"
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      TeslaFactoryBoss.stopIfNotAlreadyStopped
      _healthCheck.stop
      markNotRunning
    }
    this
  }

}
