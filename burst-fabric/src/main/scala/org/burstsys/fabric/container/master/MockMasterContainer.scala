/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.master

import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.FabricContainerId
import org.burstsys.fabric.net.FabricNetworkConfig
import org.burstsys.fabric.net.server.unitFabricNetworkServerConfig
import org.burstsys.tesla.part.factory.TeslaFactoryBoss
import org.burstsys.vitals.git
import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.burstsys.{fabric, tesla, vitals}

/**
 * a container for unit tests
 */
trait MockMasterContainer extends FabricMasterContainer

object MockMasterContainer {

  /**
   * constructor for situations where you know your container id in advance (such as unit tests)
   *
   * @param logFile
   * @param containerId
   * @return
   */
  def apply(logFile: String, containerId: FabricContainerId,
            netConfig: FabricNetworkConfig = unitFabricNetworkServerConfig): MockMasterContainer = {
    configuration.burstFabricMasterStandaloneProperty.set(true)
    vitals.configuration.configureForUnitTests()
    tesla.configuration.configureForUnitTests()
    fabric.configuration.configureForUnitTests()
    val c = MockMasterContainerContext(logFile: String, netConfig)
    c.containerId = containerId
    c
  }

}

private final case
class MockMasterContainerContext(logFile: String, netConfig: FabricNetworkConfig)
  extends FabricMasterContainerContext(netConfig) with MockMasterContainer {

  override def serviceName: String = s"mock-master-container"

  override def log4JPropertiesFileName: String = logFile

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This is the heart of the container lifecycle
   */
  override
  def start: this.type = {
    synchronized {
      ensureNotRunning
      git.turnOffBuildValidation()
      VitalsMetricsRegistry.disable()

      System.setProperty("tesla.parts.tender.frequency", 30.toString)

      // initialize as a test
      VitalsLog.configureLogging(log4JPropertiesFileName, consoleOnly = true)

      log info startingMessage
      // start the underlying fabric container
      super.start
      log info startedWithDateMessage

      markRunning
    }
    this
  }


  /**
   * This is the heart of the container lifecycle
   */
  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      log info stoppingMessage

      // stop fabric layer
      super.stop


      markNotRunning
      log info stoppedWithDateMessage

      TeslaFactoryBoss.assertNoInUseParts()
    }
    this
  }

}
