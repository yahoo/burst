/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.brio.flurry.provider.unity.BurstUnityValidator
import org.burstsys.nexus._
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.tesla
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.parcel.{TeslaParcel, _}
import org.burstsys.tesla.thread.request.{TeslaRequestCoupler, TeslaRequestFuture}
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}
import org.burstsys.vitals.properties.{BurstMotifFilter, VitalsPropertyMap}
import org.scalatest.Ignore

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@Ignore
class NexusParcelBasicSpec extends NexusSpec {


  it should "perform basic parcel send/recv" in {
    TeslaRequestCoupler {
      val guid = newNexusUid
      val suid = newNexusUid
      val parcels = BurstUnityMockData(itemCount = 100).pressToInflatedParcels
      val bufferCount = parcels.foldRight(0)(_.bufferCount + _)

      val server = grabServer(getPublicHostAddress) fedBy new NexusStreamFeeder {

        override
        def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
          fail("we should not get a parcel abort")
        }

        override def feedStream(stream: NexusStream): Unit = {
          TeslaRequestFuture {
            try {
              parcels foreach (stream put)
              stream put TeslaEndMarkerParcel
            } catch safely {
              case t: Throwable =>
                throw t
            }
          }
        }
      }

      try {
        val client = grabClientFromPool(getPublicHostAddress, server.serverPort)
        try {

          val pipe = TeslaParcelPipe(name = "mock", guid = guid, suid = suid).start
          val streamProperties: VitalsPropertyMap = Map("someKey" -> "someValue")
          val motifFilter: BurstMotifFilter = Some("someMotifFilter")
          val stream = client.startStream(guid, suid, streamProperties, "quo", motifFilter, pipe, 0, getPublicHostName, getPublicHostName)

          val results = new ArrayBuffer[TeslaParcel]

          var continue = true
          while (continue) {
            val deflatedParcel = stream.take
            if (deflatedParcel == TeslaEndMarkerParcel) {
              continue = false
            } else {
              // so we should have a deflated parcel
              deflatedParcel.isInflated should equal(false)

              // should be able to inflate the parcel
              val inflatedParcel = tesla.parcel.factory.grabParcel(deflatedParcel.inflatedSize)
              inflatedParcel.inflateFrom(deflatedParcel)

              // the inflated parcel should agree
              inflatedParcel.inflatedSize should equal(deflatedParcel.inflatedSize)
              inflatedParcel.bufferCount should equal(deflatedParcel.bufferCount)

              // read the buffers
              inflatedParcel.startReads()
              for (i <- 1 to inflatedParcel.bufferCount) {
                val bItem = inflatedParcel.readNextBuffer
                bItem.isNullBuffer should not equal true
                BurstUnityValidator.validateBlob(bItem)
                tesla.buffer.factory.releaseBuffer(bItem)
              }
              inflatedParcel.bufferCount should equal(0)
              results += deflatedParcel
            }
          }

          results foreach tesla.parcel.factory.releaseParcel

          Await.result(stream.receipt, 10 seconds)
        } finally releaseClientToPool(client)

      } finally {
        client.shutdownPool
        releaseServer(server)
      }
    }
  }
}
