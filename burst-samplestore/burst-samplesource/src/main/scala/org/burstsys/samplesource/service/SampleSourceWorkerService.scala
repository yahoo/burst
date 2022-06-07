/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.service

import org.burstsys.nexus.stream.NexusStream

import scala.concurrent.Future

/**
  * sample source handling of cell side client worker operations
  */
trait SampleSourceWorkerService extends Any {

  /**
    * feed an incoming stream request from a sample source cell side client worker
    * this is implemented by the underlying sample source associate with this request
    * @param stream
    * @return
    */
  def feedStream(stream: NexusStream): Future[Unit]

}

