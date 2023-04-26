/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test.parcel.packer

import org.burstsys.brio.flurry.provider.unity.BurstUnityMockData
import org.burstsys.nexus.test.NexusSpec
import org.burstsys.tesla
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.parcel.TeslaTimeoutMarkerParcel
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.vitals.uid.newBurstUid

import scala.concurrent.duration.DurationInt

class NexusParcelPackerSpec extends NexusSpec {

  it should "be reusable" in {
    val buffers = BurstUnityMockData(itemCount = 10000).pressToBuffers

    val (_, parcelCount1) = packBuffers(buffers)
    val (_, parcelCount2) = packBuffers(buffers)
    val (_, parcelCount3) = packBuffers(buffers)
    val (_, parcelCount4) = packBuffers(buffers)

    parcelCount1 shouldEqual parcelCount2
    parcelCount1 shouldEqual parcelCount3
    parcelCount1 shouldEqual parcelCount4
  }

  private def packBuffers(buffers: Array[TeslaMutableBuffer]): (Int, Int) = {
    val guid = newBurstUid
    val suid = newBurstUid
    val pipe = TeslaParcelPipe(name = "mock", guid = guid, suid = suid, timeout = 1.millisecond).start

    val packer = tesla.parcel.packer.grabPacker(newBurstUid, pipe)
    val packerId = packer.packerId

    buffers.foreach(packer.put)
    packer.finishWrites()
    tesla.parcel.packer.releasePacker(packer)

    (packerId, getPipeParcelCount(pipe))
  }

  private def getPipeParcelCount(pipe: TeslaParcelPipe): Int = {
    var parcelCount = 0
    var parcel = pipe.take
    while (parcel != TeslaTimeoutMarkerParcel) {
      parcelCount += 1
      tesla.parcel.factory.releaseParcel(parcel)
      parcel = pipe.take
    }
    parcelCount
  }

}
