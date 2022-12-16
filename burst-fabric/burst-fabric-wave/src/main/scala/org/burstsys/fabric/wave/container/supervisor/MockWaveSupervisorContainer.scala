/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.container.supervisor

import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.FabricContainerId
import org.burstsys.fabric.net.FabricNetworkConfig
import org.burstsys.fabric.net.server.unitFabricNetworkServerConfig
import org.burstsys.tesla.part.factory.TeslaFactoryBoss
import org.burstsys.vitals.logging._
import org.burstsys.{fabric, tesla, vitals}

/**
 * a container for unit tests
 */
trait MockWaveSupervisorContainer extends FabricWaveSupervisorContainer

object MockWaveSupervisorContainer {

  /**
   * constructor for situations where you know your container id in advance (such as unit tests)
   *
   * @param logFile
   * @param containerId
   * @return
   */
  def apply(logFile: String, containerId: FabricContainerId,
            netConfig: FabricNetworkConfig = unitFabricNetworkServerConfig): MockWaveSupervisorContainer = {
    configuration.burstFabricSupervisorStandaloneProperty.set(true)
    vitals.configuration.configureForUnitTests()
    tesla.configuration.configureForUnitTests()
    fabric.wave.configuration.configureForUnitTests()
    val c = MockWaveSupervisorContainerContext(logFile: String, netConfig)
    c.containerId = containerId
    c
  }

}

private final case
class MockWaveSupervisorContainerContext(logFile: String, netConfig: FabricNetworkConfig)
  extends FabricWaveSupervisorContainerContext(netConfig) with MockWaveSupervisorContainer {

  override def serviceName: String = s"mock-supervisor-container"

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
