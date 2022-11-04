/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor

import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.samplesource.service.MetadataParameters

import scala.concurrent.Future

/**
 * handler for supervisor container events
 */
trait FabricStoreSupervisorAPI extends AnyRef {

  def updateMetadata(connection: FabricNetServerConnection, sourceName: String, metadata: MetadataParameters): Future[Unit]

  def updateMetadata(sourceName: String): Future[Unit]
}
