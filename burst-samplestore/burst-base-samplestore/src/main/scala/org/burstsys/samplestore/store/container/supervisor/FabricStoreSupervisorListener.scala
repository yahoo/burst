/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor

import org.burstsys.fabric.container.supervisor.FabricSupervisorListener
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.samplestore.store.message.metadata.FabricStoreMetadataRespMsg

/**
 * handler for supervisor container events
 */
trait FabricStoreSupervisorListener extends FabricSupervisorListener {
  def onStoreMetadataRespMsg(connection: FabricNetServerConnection, msg: FabricStoreMetadataRespMsg): Unit = {}
}
