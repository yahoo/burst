/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.worker

import org.burstsys._
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.{CatalogUnitTestWorkerConfig, CatalogWorkerConfig}
import org.burstsys.fabric.container.FabricWorkerContainerProvider
import org.burstsys.fabric.wave.container.worker.{FabricWaveWorkerContainer, FabricWaveWorkerContainerContext}
import org.burstsys.system.test.configuration
import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

import scala.language.postfixOps

trait BurstSystemTestWaveWorkerContainer extends FabricWaveWorkerContainer

@FabricWorkerContainerProvider
final case
class BurstSystemTestWaveWorkerContainerContext() extends FabricWaveWorkerContainerContext() with BurstSystemTestWaveWorkerContainer {

  override def serviceName: String = s"burst-system-test-worker-container"

  override def log4JPropertiesFileName: String = "worker"

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
      VitalsLog.configureLogging(burstLog4j2NameProperty.get)

      configuration.configureThreading()

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
