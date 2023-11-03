/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.container

import org.burstsys.agent.AgentService
import org.burstsys.agent.processors.{BurstSystemEqlQueryProcessor, BurstSystemHydraQueryProcessor}
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.{CatalogSupervisorConfig, CatalogUnitTestServerConfig}
import org.burstsys.fabric.container.{FabricSupervisorContainerProvider, SupervisorLog4JPropertiesFileName}
import org.burstsys.fabric.net.server.defaultFabricNetworkServerConfig
import org.burstsys.fabric.wave
import org.burstsys.fabric.wave.container.supervisor.{FabricWaveSupervisorContainer, FabricWaveSupervisorContainerContext}
import org.burstsys.hydra.HydraService
import org.burstsys.supervisor
import org.burstsys.supervisor.configuration
import org.burstsys.supervisor.configuration.kedaScaleDownInterval
import org.burstsys.supervisor.http.BurstWaveHttpBinder
import org.burstsys.supervisor.http.endpoints._
import org.burstsys.supervisor.http.service.burnin.BurstWaveBurnInService
import org.burstsys.supervisor.http.service.execution.requests
import org.burstsys.supervisor.http.service.provider.BurstWaveSupervisorBurnInService
import org.burstsys.supervisor.http.service.thrift
import org.burstsys.supervisor.http.websocket.{BurstExecutionRelay, BurstThriftRelay, BurstTopologyRelay, WaveSupervisorBurnInRelay}
import org.burstsys.vitals
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostPort
import org.glassfish.hk2.utilities.binding.AbstractBinder

import scala.language.postfixOps

trait BurstWaveSupervisorContainer extends FabricWaveSupervisorContainer {

  def agent: AgentService

  def catalog: CatalogService

  def burnIn: BurstWaveSupervisorBurnInService

}

@FabricSupervisorContainerProvider
final case class BurstWaveSupervisorContainerContext()
  extends FabricWaveSupervisorContainerContext(defaultFabricNetworkServerConfig) with BurstWaveSupervisorContainer {

  override def serviceName: String = s"burst-supervisor-container"

  override def log4JPropertiesFileName: String = SupervisorLog4JPropertiesFileName

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Api
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def agent: AgentService = _agent

  override def catalog: CatalogService = _catalog

  override def burnIn: BurstWaveSupervisorBurnInService = _burnIn

  override def workersActive: Boolean = {
    val earliestAllowedRequest = System.currentTimeMillis - configuration.kedaScaleDownInterval.get.toMillis
    val systemBootTime = System.currentTimeMillis - vitals.host.uptime
    val recentlyBooted = requests.mostRecentRequest.isEmpty && systemBootTime > earliestAllowedRequest
    recentlyBooted || requests.mostRecentRequest.exists(_.startTime > earliestAllowedRequest)
  }

  override def desiredWorkerCount: Int = configuration.kedaScaleUpWorkerCount.get


  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Http
  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  override def httpBinder: AbstractBinder = new BurstWaveHttpBinder(this)

  override def httpResources: Array[Class[_]] = super.httpResources ++ Array(
    classOf[WaveSupervisorHtmlAssetEndpoint],
    classOf[WaveSupervisorCacheEndpoint],
    classOf[WaveSupervisorCatalogEndpoint],
    classOf[WaveSupervisorExecutionEndpoint],
    classOf[WaveSupervisorInfoEndpoint],
    classOf[WaveSupervisorQueryEndpoint],
    classOf[WaveSupervisorThriftEndpoint],
    classOf[WaveSupervisorBurnInEndpoint],
    classOf[BurstThriftMessageBodyWriter],
  )

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // languages
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected var _hydra: HydraService = _

  protected var _hydraProcessor: BurstSystemHydraQueryProcessor = _

  protected var _eqlProcessor: BurstSystemEqlQueryProcessor = _

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // other services
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected val _agent: AgentService = AgentService(bootModality)

  protected val _catalog: CatalogService =
    CatalogService(if (bootModality.isStandalone) CatalogUnitTestServerConfig else CatalogSupervisorConfig)

  protected val _burnIn: BurstWaveBurnInService = BurstWaveBurnInService(agent, catalog)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This is the heart of the container lifecycle
   */
  override def start: this.type = {
    try {
      synchronized {
        ensureNotRunning

        // this should be done before any other systems start up
        VitalsLog.configureLogging(burstLog4j2NameProperty.get)

        /*
         * the critical first step is to initialize the catalog and any other systems that
         * need to be injected into the web server
         */
        _catalog.start

        _agent.start

        // tell fabric layer we have basic metadata lookup capability
        metadata withLookup _catalog.metadataLookup

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
          _hydraProcessor = BurstSystemHydraQueryProcessor(_agent, _hydra)
          _agent.registerLanguage(_hydraProcessor)

          /////////////////////////////////////////////////////////////////
          // eql
          /////////////////////////////////////////////////////////////////
          _eqlProcessor = BurstSystemEqlQueryProcessor(_agent, _catalog)
          _agent.registerLanguage(_eqlProcessor)

          // agent commands
          _agent.registerCache(data)

          startWebsocketServices()

        } catch safely {
          case t: Throwable =>
            log error burstStdMsg(s"$serviceName startup failed", t)
            throw VitalsException(t)
        }

        health.registerService(_agent, _catalog, netServer)

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

    thrift.requestLog talksTo BurstThriftRelay(webSocketService)

    burnIn talksTo WaveSupervisorBurnInRelay(webSocketService, burnIn)

  }

  override def stop: this.type = {
    synchronized {
      ensureRunning
      log info stoppingMessage

      _hydra.stop
      _agent.stop

      // stop fabric layer
      super.stop

      // shutdown metadata access
      _catalog.stop

      markNotRunning
      log info stoppedWithDateMessage
    }
    this
  }

}
