/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.server.container

import org.burstsys.agent.AgentService
import org.burstsys.agent.processors.BurstSystemEqlQueryProcessor
import org.burstsys.agent.processors.BurstSystemHydraQueryProcessor
import org.burstsys.brio.provider.loadBrioSchemaProviders
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.CatalogSupervisorConfig
import org.burstsys.catalog.CatalogService.CatalogUnitTestServerConfig
import org.burstsys.dash.BurstDashService
import org.burstsys.fabric.container.{FabricSupervisorContainerProvider, SupervisorLog4JPropertiesFileName}
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainerContext
import org.burstsys.fabric.net.server.defaultFabricNetworkServerConfig
import org.burstsys.hydra.HydraService
import org.burstsys.supervisor.configuration
import org.burstsys.supervisor.configuration.burstSupervisorJsonWatchDirectoryProperty
import org.burstsys.supervisor.configuration.burstSupervisorPropertiesFileProperty
import org.burstsys.supervisor.server.torcher.BurstSupervisorTorcherService
import org.burstsys.tesla
import org.burstsys.vitals.VitalsService.VitalsStandardClient
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import java.lang.Runtime.getRuntime
import scala.language.postfixOps

trait BurstWaveSupervisorContainer extends FabricWaveSupervisorContainer {

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
  def torcher: BurstSupervisorTorcherService

  /**
   * TODO
   *
   * @return
   */
  def catalog: CatalogService

  /**
   * TODO
   *
   * @return
   */
  def restApi: BurstDashService

}

@FabricSupervisorContainerProvider final case
class BurstWaveSupervisorContainerContext() extends FabricWaveSupervisorContainerContext(defaultFabricNetworkServerConfig)
  with BurstWaveSupervisorContainer with BurstSupervisorStartup {

  override def serviceName: String = s"burst-supervisor-container"

  override def log4JPropertiesFileName: String = SupervisorLog4JPropertiesFileName

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Api
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  def agent: AgentService = _agentClient

  def torcher: BurstSupervisorTorcherService = _torcher

  def catalog: CatalogService = _catalogServer

  def restApi: BurstDashService = _rest

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REST
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected
  var _rest: BurstDashService = _

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
  val _agentClient: AgentService = AgentService(VitalsStandardClient)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Catalog
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected
  val _catalogServer: CatalogService = CatalogService(if (bootModality.isStandalone) CatalogUnitTestServerConfig else CatalogSupervisorConfig)

  protected
  val _jsonFileManager: BurstJsonFileManager = BurstJsonFileManager(catalog = catalog, modality = VitalsStandardServer)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // testing
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected
  var _torcher: BurstSupervisorTorcherService = _

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This is the heart of the container lifecycle
   *
   * @return
   */
  override def start: this.type = {
    try {
      synchronized {
        ensureNotRunning

        // this should be done before any other systems start up
        VitalsLog.configureLogging(burstLog4j2NameProperty.get)

        /*
         * the critical first step is to start up the catalog - eventually all other config info comes from this
         */
        _catalogServer.start

        _agentServer.start
        _agentClient.start

        // tell fabric layer we have basic metadata lookup capability
        metadata withLookup _catalogServer.metadataLookup

        /////////////////////////////////////////////////////////////////
        // stores
        /////////////////////////////////////////////////////////////////

        // install sample store
        if (burstSupervisorJsonWatchDirectoryProperty.asOption.isDefined && burstSupervisorPropertiesFileProperty.asOption.get.nonEmpty){
          log info s"Starting JSON File Manager for directory ${burstSupervisorJsonWatchDirectoryProperty.asOption.get}"
          _jsonFileManager.start
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
      _rest.stop

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
