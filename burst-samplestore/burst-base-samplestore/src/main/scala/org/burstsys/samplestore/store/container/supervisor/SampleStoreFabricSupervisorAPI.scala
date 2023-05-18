/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor

import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.samplesource.service.MetadataParameters

import scala.concurrent.Future

/**
 * handler for supervisor container events
 */
trait SampleStoreFabricSupervisorAPI extends AnyRef {

  /**
   * Tell a fabric worker to update the metadata for a particular sample source
   *
   * @param connection the connection to the fabric worker
   * @param sourceName the sample source that should receive the metadata
   * @param metadata   the new metadata for the sample source
   * @return A future that completes when the message has been sent to the fabric worker
   */
  def updateMetadata(connection: FabricNetServerConnection, sourceName: String, metadata: MetadataParameters): Future[Unit]

  /**
   * Initiate a metadata push to all fabric workers for a particular sample source
   *
   * @param sourceName the name of the sample source to push to
   * @return A future that completes when messages have been sent to all of the fabric workers
   */
  def updateMetadata(sourceName: String): Future[Unit]

}
