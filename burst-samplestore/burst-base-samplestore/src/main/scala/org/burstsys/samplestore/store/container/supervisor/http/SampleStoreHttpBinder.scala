/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor.http

import org.burstsys.fabric.container.http.{FabricAuthorizationProvider, FabricHttpBinder}
import org.burstsys.fabric.topology.supervisor.FabricSupervisorTopology
import org.burstsys.samplestore.store.container.supervisor.http.SampleStoreHttpBinder.storeListenerProperties
import org.burstsys.samplestore.store.container.supervisor.{SampleStoreFabricSupervisorContainer, SampleStoreFabricSupervisorContainerContext}
import org.burstsys.samplestore.store.container.supervisor.http.services.ViewGenerationRequestLog
import org.burstsys.vitals.properties.VitalsPropertyMap

object SampleStoreHttpBinder {
  val storeListenerProperties = "storeListenerProperties"
}

class SampleStoreHttpBinder(
                             container: SampleStoreFabricSupervisorContainer,
                             topology: FabricSupervisorTopology,
                             requestLog: ViewGenerationRequestLog,
                             storeListenerProperties: VitalsPropertyMap,
                           ) extends FabricHttpBinder(container) {

  override protected def authorizer: FabricAuthorizationProvider = new SampleStoreHttpAuthorizer()

  override def configure(): Unit = {
    super.configure()
    bind(container).to(classOf[SampleStoreFabricSupervisorContainer])
    bind(topology).to(classOf[FabricSupervisorTopology])
    bind(requestLog).to(classOf[ViewGenerationRequestLog])
    bind(storeListenerProperties).to(classOf[VitalsPropertyMap]).named(SampleStoreHttpBinder.storeListenerProperties)
  }

}
