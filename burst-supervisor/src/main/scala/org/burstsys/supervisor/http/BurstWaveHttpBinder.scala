/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http

import org.apache.thrift.protocol.{TBinaryProtocol, TProtocolFactory}
import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.fabric.container.http.{FabricAuthorizationProvider, FabricHttpBinder}
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.gen.thrift.api.client.BTBurstService
import org.burstsys.gen.thrift.api.client.BTBurstService.Iface
import org.burstsys.supervisor.container.BurstWaveSupervisorContainer
import org.burstsys.supervisor.http.service.thrift

class BurstWaveHttpBinder(
                           container: BurstWaveSupervisorContainer,
                         ) extends FabricHttpBinder(container) {

  override protected def authorizer: FabricAuthorizationProvider = new BurstWaveCatalogAuthorizer(container.catalog)

  override def configure(): Unit = {
    super.configure()
    bind(container).to(classOf[FabricWaveSupervisorContainer])
    bind(container.agent).to(classOf[AgentService])
    bind(container.catalog).to(classOf[CatalogService])
    bindFactory(() => thrift.processor(container.catalog, container.agent)).to(classOf[BTBurstService.Processor[Iface]])
    bindFactory(() => new TBinaryProtocol.Factory()).to(classOf[TProtocolFactory])
  }

}
