/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import org.burstsys.brio.flurry.provider.quo.BurstQuoMockData
import org.burstsys.nexus._
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.tesla
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName
import org.burstsys.vitals.properties._

import java.util.concurrent.CountDownLatch
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

//@Ignore
class NexusParcelRateLimitSpec extends NexusSpec {

  it should "efficiently rate limit with parcels" in {
    TeslaRequestCoupler {
      val parcels = new ArrayBuffer[TeslaParcel]
      parcels ++= BurstQuoMockData(userCount = 1e3.toInt).pressToInflatedParcels
      val parcelCount = parcels.size
      val guid = newNexusUid
      val writesDoneGate = new CountDownLatch(1)
      val server = grabServer(getPublicHostAddress) fedBy new NexusStreamFeeder {
        override def feedStream(stream: NexusStream): Unit = {
          TeslaRequestFuture {
            try {
              for (i <- 0 until parcelCount) {
                stream put parcels.remove(0)
              }
              stream put TeslaEndMarkerParcel
              writesDoneGate.countDown()
            } catch safely {
              case t: Throwable =>
                log error t
                fail(s"got exception:$t")
            }
          }
        }

        override
        def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
          fail("we should not get a parcel abort")
        }

      }

      try {
        val client = grabClientFromPool(getPublicHostAddress, server.serverPort)
        try {
          val suid = newNexusUid
          val pipe = TeslaParcelPipe(name = "mock", guid = guid, suid = suid).start
          val streamProperties: VitalsPropertyMap = Map("someKey" -> "someValue")
          val motifFilter: BurstMotifFilter = Some("someMotifFilter")
          val stream = client.startStream(guid, suid, streamProperties, "quo", motifFilter, pipe, 0, getPublicHostName, getPublicHostName)
          var continue = true
          while (continue) {
            val parcel = pipe.take
            if (parcel == TeslaEndMarkerParcel) {
              continue = false
            } else if (parcel == TeslaTimeoutMarkerParcel) {
              throw VitalsException(s"TeslaParcelPipeTimeout")
              continue = false
            } else {
              tesla.parcel.factory releaseParcel parcel
            }
          }
          writesDoneGate.await()
          Await.result(stream.receipt, 60 seconds)
        } finally releaseClientToPool(client)

      } finally {
        client.shutdownPool
        releaseServer(server)
      }
    }
  }

}
