/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel.streams

import org.burstsys.brio.flurry.provider.unity.{BurstUnityMockData, BurstUnityValidator}
import org.burstsys.nexus._
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.test.{NexusParcelStreamSpec, NoAbortStreamFeeder}
import org.burstsys.tesla
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.thread.request.{TeslaRequestCoupler, TeslaRequestFuture}
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class NexusParcelPackerBasicSpec extends NexusParcelStreamSpec {

  private val itemCount = 100

  override def feeder: NexusStreamFeeder = new NoAbortStreamFeeder {
    override def feedStream(stream: NexusStream): Unit = {
      TeslaRequestFuture {
        BurstUnityMockData(itemCount).pressToBuffers.foreach(stream.put)
        stream.complete(itemCount, itemCount, itemCount, 0)
      }
    }
  }


  "Nexus streams" should "perform basic buffer send/receive using parcel packer" in {
    TeslaRequestCoupler {
      val guid = newNexusUid
      val stream = startStream(guid)

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

      stream.itemCount shouldEqual itemCount
      stream.expectedItemCount shouldEqual itemCount
      stream.potentialItemCount shouldEqual itemCount
      stream.rejectedItemCount shouldEqual 0

      receivedBufferCount should equal(itemCount)

      Await.result(stream.completion, 10 seconds)
    }
  }
}
