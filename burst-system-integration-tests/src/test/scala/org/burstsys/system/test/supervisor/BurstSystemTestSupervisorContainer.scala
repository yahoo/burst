/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.supervisor

import org.burstsys.agent.AgentService
import org.burstsys.agent.processors.BurstSystemEqlQueryProcessor
import org.burstsys.agent.processors.BurstSystemHydraQueryProcessor
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.CatalogSupervisorConfig
import org.burstsys.catalog.CatalogService.CatalogUnitTestServerConfig
import org.burstsys.fabric.container.FabricSupervisorContainerProvider
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainerContext
import org.burstsys.fabric.net.server.defaultFabricNetworkServerConfig
import org.burstsys.hydra.HydraService
import org.burstsys.nexus
import org.burstsys.system.test.configuration
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.zap

import scala.language.postfixOps

trait BurstSystemTestSupervisorContainer extends FabricWaveSupervisorContainer {

  /**
   * TODO
   *
   * @return
   */
  def agent: AgentService

  /**
   * TODO
   *
   * @return
   */
  def catalog: CatalogService

}

@FabricSupervisorContainerProvider final case
class BurstSystemTestSupervisorContainerContext() extends FabricWaveSupervisorContainerContext(defaultFabricNetworkServerConfig)
  with BurstSystemTestSupervisorContainer with BurstSystemTestSupervisorStartup {

  override def serviceName: String = s"burst-system-test-supervisor-container"

  override def log4JPropertiesFileName: String = "supervisor"

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Api
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  def agent: AgentService = _agentClient

  def catalog: CatalogService = _catalogServer

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // languages
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected
  var _hydra: HydraService = _

  protected
  var _hydraProcessor: BurstSystemHydraQueryProcessor = _

  protected
  var _eqlProcessor: BurstSystemEqlQueryProcessor = _

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Agent
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected
  val _agentServer: AgentService = AgentService(bootModality)

  protected
  val _agentClient: AgentService = AgentService()

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Catalog
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected
  val _catalogServer: CatalogService = CatalogService(if (bootModality.isStandalone) CatalogUnitTestServerConfig else CatalogSupervisorConfig)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This is the heart of the container lifecycle
   *
   * @return
   */
  override
  def start: this.type = {
    try {
      synchronized {
        ensureNotRunning

        // this should be done before any other systems start up
        VitalsLog.configureLogging(burstLog4j2NameProperty.get)

        // assert resource minimums
        configuration.configureThreading()

        /**
         * the critical first step is to start up the catalog - eventually all other config info comes from this
         */
        _catalogServer.start

        // tell fabric layer we have basic metadata lookup capability
        metadata withLookup _catalogServer.metadataLookup

        /////////////////////////////////////////////////////////////////
        // stores
        /////////////////////////////////////////////////////////////////

        // install sample store
        if (bootModality.isStandalone) {
          log info s"BURST_SUPERVISOR_STANDALONE_MODE $serviceName"
        }

        /**
         * now that we have defined metadata lookup and stores - now we can start the underlying fabric layer container services
         */
        super.start

        log info startingMessage

        // ready to start up top level container...
        startForeground

        health.registerService(_agentServer, _catalogServer, netServer)

        log info startedWithDateMessage
        markRunning
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
    this
  }


  /**
   * This is the heart of the container lifecycle
   *
   * @return
   */
  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      log info stoppingMessage

      _hydra.stop
      _agentServer.stop
      _agentClient.stop

      // stop fabric layer
      super.stop

      // shutdown metadata access
      _catalogServer.stop

      markNotRunning
      log info stoppedWithDateMessage
    }
    this
  }

}
