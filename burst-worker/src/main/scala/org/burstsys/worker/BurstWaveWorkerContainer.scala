/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.worker

import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.{CatalogUnitTestWorkerConfig, CatalogWorkerConfig}
import org.burstsys.fabric.wave.container.worker.{FabricWaveWorkerContainer, FabricWaveWorkerContainerContext}
import org.burstsys.fabric.container.WorkerLog4JPropertiesFileName
import org.burstsys.fabric.container.FabricWorkerContainerProvider
import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

import scala.annotation.unused
import scala.language.postfixOps

trait BurstWaveWorkerContainer extends FabricWaveWorkerContainer

@FabricWorkerContainerProvider
@unused // found by reflection
final case
class BurstWaveWorkerContainerContext() extends FabricWaveWorkerContainerContext() with BurstWaveWorkerContainer {

  override def serviceName: String = s"burst-worker-container"

  override def log4JPropertiesFileName: String = WorkerLog4JPropertiesFileName

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Private State
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[worker]
  var _catalogServer: CatalogService = _

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    try {
      ensureNotRunning

      // this should be done before any other systems start up
      VitalsLog.configureLogging(burstLog4j2NameProperty.getOrThrow)

      log info startingMessage

      // start catalog
      _catalogServer = CatalogService(
        if (bootModality == VitalsStandaloneServer) CatalogUnitTestWorkerConfig else CatalogWorkerConfig
      ).start

      // start fabric layer
      super.start

      health.registerService(_catalogServer, _netClient, engine)

      log info startedWithDateMessage

      markRunning
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage

    // stop catalog
    _catalogServer.stop

    // stop fabric layer
    super.stop

    markNotRunning
    log info stoppedWithDateMessage
    this
  }

}
