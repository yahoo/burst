/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor

import org.burstsys.fabric.container.supervisor.FabricSupervisorListener
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.samplestore.store.message.metadata.FabricStoreMetadataRespMsg

/**
 * Handle samplestore related fabric events
 */
trait SampleStoreFabricSupervisorListener extends FabricSupervisorListener {
  /**
   * Notify the store supervisor that a worker has sent a metadata response
   */
  def onStoreMetadataRespMsg(connection: FabricNetServerConnection, msg: FabricStoreMetadataRespMsg): Unit = {}
}
