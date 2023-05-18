/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.worker

import org.burstsys.fabric.container.{FabricWorkerContainerProvider, WorkerLog4JPropertiesFileName}
import org.burstsys.fabric.net.server.defaultFabricNetworkServerConfig
import org.burstsys.fabric.wave.container.worker.{FabricWaveWorkerContainer, FabricWaveWorkerContainerContext}
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

import scala.annotation.unused
import scala.language.postfixOps

trait BurstWaveWorkerContainer extends FabricWaveWorkerContainer

@FabricWorkerContainerProvider
@unused // found by reflection
final case
class BurstWaveWorkerContainerContext() extends FabricWaveWorkerContainerContext(defaultFabricNetworkServerConfig) with BurstWaveWorkerContainer {

  override def serviceName: String = s"burst-worker-container"

  override def log4JPropertiesFileName: String = WorkerLog4JPropertiesFileName

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    try {
      ensureNotRunning

      // this should be done before any other systems start up
      VitalsLog.configureLogging(burstLog4j2NameProperty.get)

      log info startingMessage

      // start fabric layer
      super.start

      health.registerService(_netClient, engine)

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

    // stop fabric layer
    super.stop

    markNotRunning
    log info stoppedWithDateMessage
    this
  }

}
