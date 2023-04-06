/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.parcel

import org.burstsys.tesla.thread.request._
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.tesla.test.support.TeslaAbstractSpec
import org.burstsys.tesla.thread.request._
import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.burstsys.vitals.uid._
import org.burstsys.{tesla, vitals}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

//@Ignore
class TeslaParcelPackerSpec extends TeslaAbstractSpec {

  it should "pack buffers into a parcel" in {

    vitals.reporter.startReporterSystem()

    TeslaRequestCoupler {

      val mockGuid = ""
      val mockSuid = ""
      val bufferCount = 1e6.toInt
      val bufferSize = 1e3.toInt
      val parcelCount = 4
      val concurrency = 4

      val pipe = TeslaParcelPipe(name = "mockpipe", guid = mockGuid, suid = mockSuid, depth = 10).start

      val packer = tesla.parcel.packer.grabPacker(newBurstUid, pipe)
      try {
        val futures = for (f <- 0 until concurrency) yield {
          TeslaWorkerFuture {
            for (_ <- 0 until bufferCount / concurrency) {
              val buffer = tesla.buffer.factory.grabBuffer(bufferSize)
              for (i <- 0 until bufferSize) buffer.writeByte(i.toByte)
              packer.put(buffer)
            }
          }
        }
        Await.result(Future.sequence(futures), 5 minutes)
      } finally tesla.parcel.packer.releasePacker(packer)

      (0 until parcelCount).foreach { _ =>
        tesla.parcel.factory.releaseParcel(pipe.take)
      }

    }

  }

}
