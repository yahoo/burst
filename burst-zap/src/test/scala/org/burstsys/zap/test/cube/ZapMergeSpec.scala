/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube

import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.zap.cube.{ZapCubeBuilder, ZapCubeContext}
import org.burstsys.zap.test.ZapAbstractSpec
import org.burstsys.{brio, tesla}
import org.scalatest.Ignore

@Ignore
class ZapMergeSpec extends ZapAbstractSpec {


  import ZapTestData._

  private val builder =  ZapCubeBuilder().init(0, "no_frame", null)

  "Zap Merge" should "work" in {
    TeslaWorkerCoupler {

      val block = tesla.block.factory.grabBlock(s.totalMemorySize)
      val dictionary = brio.dictionary.factory.grabMutableDictionary()
      val cube = new ZapCubeContext(block.blockBasePtr, -1)
      val map1 = cube.initialize(builder, cube)

      val map2 = cube.initialize(builder, cube)
    }


  }


}
