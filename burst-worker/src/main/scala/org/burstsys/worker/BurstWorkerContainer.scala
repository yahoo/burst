/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.worker

import org.burstsys._
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.CatalogUnitTestWorkerConfig
import org.burstsys.catalog.CatalogService.CatalogWorkerConfig
import org.burstsys.fabric.container.FabricWorkerContainerProvider
import org.burstsys.fabric.container.WorkerLog4JPropertiesFileName
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.container.worker.FabricWorkerContainerContext
import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

import scala.language.postfixOps

trait BurstWorkerContainer extends FabricWorkerContainer

@FabricWorkerContainerProvider
final case
class BurstWorkerContainerContext() extends FabricWorkerContainerContext() with BurstWorkerContainer {

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

      // tell fabric layer we have basic metadata lookup capability
      metadata withLookup _catalogServer.metadataLookup

      // start fabric layer
      super.start

      health.registerService(_catalogServer, netClient, engine)

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
