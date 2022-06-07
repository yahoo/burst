/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import org.burstsys.nexus.test.NexusSpec
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.instrument.VitalsCounter
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


  it should "record metrics in global mode" in {
    TeslaRequestCoupler {

      val m1 = VitalsCounter("foo", 5, global = true)
      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null

      val s1 = m1.recordBytes(50)
      s1 should include("foos=5")
      s1 should include("fooBytes=250")
      m1.recordBytes(50) shouldEqual null

      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null

      val s2 = m1.recordBytes(50)
      s2 should include("foos=10")
      s2 should include("fooBytes=500")

      m1.recordBytes(50) shouldEqual null
    }

  }
  it should "record metrics in local mode" in {
    TeslaRequestCoupler {

      val m1 = VitalsCounter("foo", 5)
      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null

      val s1 = m1.recordBytes(50)
      s1 should include("foos=5")
      s1 should include("fooBytes=250")

      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null
      m1.recordBytes(50) shouldEqual null

      val s2 = m1.recordBytes(50)
      s2 should include("foos=5")
      s2 should include("fooBytes=250")

      m1.recordBytes(50) shouldEqual null
    }

  }


}
