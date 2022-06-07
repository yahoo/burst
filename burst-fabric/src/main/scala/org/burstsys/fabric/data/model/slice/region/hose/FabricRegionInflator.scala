/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.slice.region.hose

import org.burstsys.tesla
import org.burstsys.tesla.director.TeslaDirector
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.concurrent.{Future, Promise}


trait FabricRegionInflator extends Any {

  /**
   * inflate a parcel into a direct buffer [[TeslaDirector]]
   *
   * @param parcel
   * @return
   */
  def inflate(parcel: TeslaParcel): Future[TeslaDirector] = {
    lazy val tag = s"FabricRegionInflator.inflate(blockPtr=${parcel.blockPtr})"
    if (parcel.isInflated)
      throw VitalsException(s"ALREADY_INFLATED $tag")
    val promise = Promise[TeslaDirector]
    TeslaWorkerFuture {
      val director = tesla.director.factory.grabDirector(parcel.inflatedSize)
      try {
        val inflateStart = System.nanoTime
        parcel.inflateTo(director.payloadPtr)
        FabricHoseReporter.sampleParcelInflate(System.nanoTime - inflateStart, parcel.deflatedSize, parcel.inflatedSize)
        val buffer = director.directBuffer
        buffer limit parcel.inflatedSize
        promise.success(director)
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(s"FAIL $t $tag")
          promise.failure(t)
          tesla.director.factory.releaseDirector(director)
      }
    }
    promise.future
  }

}
