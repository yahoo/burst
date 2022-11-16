/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import org.burstsys.nexus.test.NexusSpec
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.uid.newBurstUid

import scala.concurrent.duration._
import scala.language.postfixOps

//@Ignore
class NexusParcelPipeSpec extends NexusSpec {

  it should "handle parcel pipe timeout" in {
    TeslaRequestCoupler {
      val pipe = TeslaParcelPipe(name = "mock", guid = newBurstUid, suid = newBurstUid, timeout = 1 seconds)
      pipe.start
      try {
        val value = pipe.take
        value should equal(TeslaTimeoutMarkerParcel)
      } catch safely {
        case t: Throwable =>
          throw t
      }
    }
  }
}
