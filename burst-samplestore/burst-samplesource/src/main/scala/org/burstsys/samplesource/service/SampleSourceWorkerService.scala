/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.service

import org.burstsys.nexus.stream.NexusStream
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsStandardServer

import scala.concurrent.Future

/**
 * sample source handling of cell side client worker operations
 */
trait SampleSourceWorkerService extends VitalsService {

  override def modality: VitalsService.VitalsServiceModality = VitalsStandardServer

  override def start: this.type = {
    ensureNotRunning
    log info startingMessage
    markRunning
  }

  override def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    markNotRunning
  }

  /**
   * @return The name of this sample source worker. Should probably match the name in the [[SampleSourceService]]
   */
  def name: String

  /**
   * Feed an incoming stream request from a sample source cell side client worker,
   * this is implemented by the underlying sample source associate with this request.
   *
   * Invocations of this method should be thread safe as a single instance handles all incoming requests.
   *
   * @param stream the stream of the incoming request
   * @return a future that completes when the stream has been fed
   */
  def feedStream(stream: NexusStream): Future[Unit]

  /**
   * Accept new broadcast parameters that may be needed for processing
   * @param metadata
   */
  def putBroadcastVars(metadata: MetadataParameters): Unit = {

  }
}

