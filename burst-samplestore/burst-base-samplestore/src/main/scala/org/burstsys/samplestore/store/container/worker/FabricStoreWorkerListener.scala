/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.worker

import org.burstsys.fabric.container.worker.FabricWorkerListener
import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.samplestore.store.message.metadata.FabricStoreMetadataReqMsg

/**
 * event handler for worker container events
 */
trait FabricStoreWorkerListener extends FabricWorkerListener {

  def onStoreMetadataReqMsg(connection: FabricNetClientConnection, msg: FabricStoreMetadataReqMsg): Unit = {}

}
