/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test

import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.tesla.parcel.TeslaParcelStatus
import org.scalatest.Assertions.fail

abstract class NoAbortStreamFeeder extends NexusStreamFeeder {

  override def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
    fail("we should not get a parcel abort")
  }
}
