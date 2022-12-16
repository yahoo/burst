/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http

import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.protocol.TProtocolFactory
import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.fabric.container.http.FabricAuthorizationProvider
import org.burstsys.fabric.container.http.FabricHttpBinder
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.gen.thrift.api.client.BTBurstService
import org.burstsys.gen.thrift.api.client.BTBurstService.Iface
import org.burstsys.supervisor.container.BurstWaveSupervisorContainer
import org.burstsys.supervisor.http.service.provider.BurstWaveSupervisorProfilerService
import org.burstsys.supervisor.http.service.provider.BurstWaveSupervisorTorcherService
import org.burstsys.supervisor.http.service.thrift

class BurstWaveHttpBinder(
                           agent: AgentService,
                           catalog: CatalogService,
                           container: BurstWaveSupervisorContainer,
                           profiler: BurstWaveSupervisorProfilerService,
                           torcher: BurstWaveSupervisorTorcherService
                         ) extends FabricHttpBinder(container) {

  override protected def authorizer: FabricAuthorizationProvider = new BurstWaveCatalogAuthorizer(catalog)

  override def configure(): Unit = {
    super.configure()
    bind(agent).to(classOf[AgentService])
    bind(catalog).to(classOf[CatalogService])
    bind(torcher).to(classOf[BurstWaveSupervisorTorcherService])
    bind(container).to(classOf[FabricWaveSupervisorContainer])
    bind(profiler).to(classOf[BurstWaveSupervisorProfilerService])
    bindFactory(() => thrift.processor(catalog, agent)).to(classOf[BTBurstService.Processor[Iface]])
    bindFactory(() => new TBinaryProtocol.Factory()).to(classOf[TProtocolFactory])
  }

}
