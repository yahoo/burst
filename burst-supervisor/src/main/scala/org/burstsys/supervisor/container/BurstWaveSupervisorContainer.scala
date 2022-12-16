/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.container

import org.burstsys.agent.AgentService
import org.burstsys.agent.processors.BurstSystemEqlQueryProcessor
import org.burstsys.agent.processors.BurstSystemHydraQueryProcessor
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.CatalogSupervisorConfig
import org.burstsys.catalog.CatalogService.CatalogUnitTestServerConfig
import org.burstsys.fabric.container.FabricSupervisorContainerProvider
import org.burstsys.fabric.container.SupervisorLog4JPropertiesFileName
import org.burstsys.fabric.net.server.defaultFabricNetworkServerConfig
import org.burstsys.fabric.wave
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainerContext
import org.burstsys.hydra.HydraService
import org.burstsys.supervisor.http.BurstWaveHttpBinder
import org.burstsys.supervisor.http.endpoints.BurstThriftMessageBodyWriter
import org.burstsys.supervisor.http.endpoints.WaveSupervisorCacheEndpoint
import org.burstsys.supervisor.http.endpoints.WaveSupervisorCatalogEndpoint
import org.burstsys.supervisor.http.endpoints.WaveSupervisorExecutionEndpoint
import org.burstsys.supervisor.http.endpoints.WaveSupervisorHtmlAssetEndpoint
import org.burstsys.supervisor.http.endpoints.WaveSupervisorInfoEndpoint
import org.burstsys.supervisor.http.endpoints.WaveSupervisorProfilerEndpoint
import org.burstsys.supervisor.http.endpoints.WaveSupervisorQueryEndpoint
import org.burstsys.supervisor.http.endpoints.WaveSupervisorThriftEndpoint
import org.burstsys.supervisor.http.endpoints.WaveSupervisorTorcherEndpoint
import org.burstsys.supervisor.http.service.profiler.BurstWaveProfiler
import org.burstsys.supervisor.http.service.provider.BurstWaveSupervisorProfilerService
import org.burstsys.supervisor.http.service.thrift
import org.burstsys.supervisor.http.websocket.BurstExecutionRelay
import org.burstsys.supervisor.http.websocket.BurstProfilerRelay
import org.burstsys.supervisor.http.websocket.BurstThriftRelay
import org.burstsys.supervisor.http.websocket.BurstTopologyRelay
import org.burstsys.supervisor.http.websocket.BurstTorcherRelay
import org.burstsys.supervisor.torcher.BurstSupervisorTorcherService
import org.burstsys.vitals.VitalsService.VitalsStandardClient
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig

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

}

@FabricSupervisorContainerProvider
final case class BurstWaveSupervisorContainerContext()
  extends FabricWaveSupervisorContainerContext(defaultFabricNetworkServerConfig) with BurstWaveSupervisorContainer {

  override def serviceName: String = s"burst-supervisor-container"

  override def log4JPropertiesFileName: String = SupervisorLog4JPropertiesFileName

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Api
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  def agent: AgentService = _agentClient

  def torcher: BurstSupervisorTorcherService = _torcher

  def catalog: CatalogService = _catalogServer

  def profiler: BurstWaveSupervisorProfilerService = _profiler

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Http
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def httpBinder: AbstractBinder = new BurstWaveHttpBinder(agent, catalog, this, profiler, torcher)
  
  override def httpResources: Array[Class[_]] = super.httpResources ++ Array(
    classOf[WaveSupervisorHtmlAssetEndpoint],
    classOf[WaveSupervisorCacheEndpoint],
    classOf[WaveSupervisorCatalogEndpoint],
    classOf[WaveSupervisorExecutionEndpoint],
    classOf[WaveSupervisorInfoEndpoint],
    classOf[WaveSupervisorProfilerEndpoint],
    classOf[WaveSupervisorQueryEndpoint],
    classOf[WaveSupervisorTorcherEndpoint],
    classOf[WaveSupervisorThriftEndpoint],
    classOf[BurstThriftMessageBodyWriter],
  )

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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // testing
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected
  var _torcher: BurstSupervisorTorcherService = _

  protected
  lazy val _profiler: BurstWaveProfiler = BurstWaveProfiler(agent, catalog)

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
         * the critical first step is to start up the catalog
         */
        _catalogServer.start

        _agentServer.start
        _agentClient.start

        // tell fabric layer we have basic metadata lookup capability
        metadata withLookup _catalogServer.metadataLookup

        _torcher = BurstSupervisorTorcherService(_agentServer, _catalogServer)

        /**
         * now that we have defined metadata lookup - now we can start the underlying fabric layer container services
         */
        super.start

        log info startingMessage

        try {
          /////////////////////////////////////////////////////////////////
          // hydra
          /////////////////////////////////////////////////////////////////
          _hydra = HydraService(this).start
          _hydraProcessor = BurstSystemHydraQueryProcessor(_agentServer, _hydra)
          _agentServer.registerLanguage(_hydraProcessor)

          /////////////////////////////////////////////////////////////////
          // eql
          /////////////////////////////////////////////////////////////////
          _eqlProcessor = BurstSystemEqlQueryProcessor(_agentServer, _catalogServer)
          _agentServer.registerLanguage(_eqlProcessor)

          // agent commands
          _agentServer.registerCache(data)

          startWebsocketServices()

        } catch safely {
          case t: Throwable =>
            log error burstStdMsg(s"$serviceName startup failed", t)
            throw VitalsException(t)
        }

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

  private def startWebsocketServices(): Unit = {
    topology talksTo BurstTopologyRelay(topology, webSocketService)

    wave.execution.model.pipeline addPipelineSubscriber BurstExecutionRelay(webSocketService)

    _torcher talksTo BurstTorcherRelay(_torcher, webSocketService)

    _profiler talksTo BurstProfilerRelay(_profiler, webSocketService)

    thrift.requestLog talksTo BurstThriftRelay(webSocketService)

  }

  /**
   * This is the heart of the container lifecycle
   *
   * @return
   */
  override def stop: this.type = {
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
