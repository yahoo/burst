/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application

import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.protocol.TProtocolFactory
import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.dash.application.BurstDashJson.BurstDashJacksonFeature
import org.burstsys.dash.application.BurstDashJson.BurstDashJacksonProvider
import org.burstsys.dash.configuration
import org.burstsys.dash.endpoints.ServiceStatus
import org.burstsys.dash.endpoints.assets.BurstDashStaticView
import org.burstsys.dash.endpoints.cache.BurstDashCacheRest
import org.burstsys.dash.endpoints.catalog.BurstDashCatalogRest
import org.burstsys.dash.endpoints.execution.BurstDashExecutionRest
import org.burstsys.dash.endpoints.info.BurstDashInfoRest
import org.burstsys.dash.endpoints.profiler.BurstDashProfilerRest
import org.burstsys.dash.endpoints.query.BurstDashQueryRest
import org.burstsys.dash.endpoints.thrift.BurstDashThriftEndpoint
import org.burstsys.dash.endpoints.thrift.BurstThriftMessageBodyWriter
import org.burstsys.dash.endpoints.torcher.BurstDashTorcherRest
import org.burstsys.dash.provider.profiler.BurstDashProfilerService
import org.burstsys.dash.provider.torcher.BurstDashTorcherService
import org.burstsys.dash.service.thrift
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.gen.thrift.api.client.BTBurstService
import org.burstsys.gen.thrift.api.client.BTBurstService.Iface
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.internal.monitoring.MonitoringFeature

class BurstDashApplication(
                            agent: AgentService,
                            catalog: CatalogService,
                            supervisor: FabricWaveSupervisorContainer,
                            profiler: BurstDashProfilerService,
                            torcher: BurstDashTorcherService
                          ) extends ResourceConfig {

  private val binder: AbstractBinder = new AbstractBinder {
    override def configure(): Unit = {
      bind(catalog).to(classOf[CatalogService])
      bind(torcher).to(classOf[BurstDashTorcherService])
      bind(agent).to(classOf[AgentService])
      bind(supervisor).to(classOf[FabricWaveSupervisorContainer])
      bind(profiler).to(classOf[BurstDashProfilerService])
      bindFactory(() => thrift.processor(catalog, agent)).to(classOf[BTBurstService.Processor[Iface]])
      bindFactory(() => new TBinaryProtocol.Factory()).to(classOf[TProtocolFactory])
    }
  }

  setApplicationName(configuration.burstRestNameProperty.getOrThrow)

  // setup DI
  registerInstances(binder)

  // 3rd-party features
  register(classOf[MonitoringFeature])

  // burst features and filters
  register(classOf[BurstDashJacksonFeature])
  register(classOf[BurstDashJacksonProvider])
  register(classOf[BurstThriftMessageBodyWriter])
  register(classOf[BurstDashCorsFilter])
  register(classOf[BurstDashSecurityFilter])

  register(classOf[BurstDashExceptionMapper])

  // register jax-rs resources
  registerClasses(
    classOf[BurstDashStaticView],
    classOf[BurstDashCacheRest],
    classOf[BurstDashCatalogRest],
    classOf[BurstDashExecutionRest],
    classOf[BurstDashInfoRest],
    classOf[BurstDashProfilerRest],
    classOf[BurstDashQueryRest],
    classOf[BurstDashTorcherRest],
    classOf[BurstDashThriftEndpoint],
    classOf[ServiceStatus]
  )
}
