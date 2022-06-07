/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.server

import org.burstsys.nexus.stream.NexusStream
import org.burstsys.tesla.parcel.TeslaParcelStatus

/**
  * Servers need a feeder to act as a source for streams to clients. Server side code uses this to
  * handle incoming stream requests.
  */
trait NexusStreamFeeder extends Any {

  /**
    * start feeding initiated stream
    *
    * @param stream
    * @return
    */
  def feedStream(stream: NexusStream): Unit

  /**
    * Stop feeding the stream and send abort marker
    *
    * @param stream
    */
  def abortStream(stream: NexusStream, status: TeslaParcelStatus): Unit

}
