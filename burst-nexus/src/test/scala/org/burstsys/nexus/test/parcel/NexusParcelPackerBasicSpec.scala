/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.brio.flurry.provider.unity.BurstUnityValidator
import org.burstsys.nexus._
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.endMarkerMutableBuffer
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName
import org.burstsys.vitals.properties.BurstMotifFilter
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class NexusParcelPackerBasicSpec extends NexusSpec {

  it should "perform basic buffer send/recv using parcel packer" in {
    TeslaRequestCoupler {
      val guid = newNexusUid
      val suid = newNexusUid
      val buffers = BurstUnityMockData(itemCount = 100).pressToBuffers
      val bufferCount = buffers.length

      val server = grabServer(getPublicHostAddress) fedBy new NexusStreamFeeder {

        override
        def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
          fail("we should not get a parcel abort")
        }

        override def feedStream(stream: NexusStream): Unit = {
          TeslaRequestFuture {
            try {
              buffers foreach (stream put)
              Thread.sleep(150)
              stream.complete(bufferCount, bufferCount, bufferCount, 0)
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

          var continue = true
          var receivedBufferCount = 0
          while (continue) {
            val deflatedParcel = stream.take
            TeslaWorkerCoupler {
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
                receivedBufferCount += inflatedParcel.bufferCount

                // read the buffers
                inflatedParcel.startReads()
                for (_ <- 1 to inflatedParcel.bufferCount) {
                  val bItem = inflatedParcel.readNextBuffer
                  bItem.isNullBuffer should not equal true
                  BurstUnityValidator.validateBlob(bItem)
                  tesla.buffer.factory.releaseBuffer(bItem)
                }
                inflatedParcel.bufferCount should equal(0)
                tesla.parcel.factory.releaseParcel(deflatedParcel)
              }
            }
          }
          receivedBufferCount should equal(bufferCount)

          stream.itemCount shouldEqual bufferCount
          stream.expectedItemCount  shouldEqual bufferCount
          stream.potentialItemCount shouldEqual bufferCount
          stream.rejectedItemCount shouldEqual 0

          Await.result(stream.receipt, 10 seconds)
        } finally releaseClientToPool(client)

      } finally {
        client.shutdownPool
        releaseServer(server)
      }
    }
  }
}
