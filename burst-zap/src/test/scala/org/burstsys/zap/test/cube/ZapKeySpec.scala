/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube

import org.burstsys.brio
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.felt.model.collectors.cube.FeltCubeBuilder
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.zap.cube.ZapCubeBuilder
import org.burstsys.zap.test.ZapAbstractSpec
import org.scalatest.Ignore

@Ignore
class ZapKeySpec extends ZapAbstractSpec {

  import ZapTestData._

  private val dictionary: BrioMutableDictionary = TeslaWorkerCoupler(brio.dictionary.factory.grabMutableDictionary())

  private val builder =  ZapCubeBuilder().init(frameId = 0, frameName = "no_frame", binding = null)

  "Zap Keys" should "initialize correctly" in {
    initData()
    k1.data should equal(Array[Long](3, 2, 3))
    k2.data should equal(Array[Long](1, 2, 0))
    k3.data should equal(Array[Long](0, 0, 0))
    k4.data should equal(Array[Long](7, 22, 33, 66))
  }

  "Zap rows" should "display addresses/offsets" in {
    initData()
    log info z1.printCube(builder, z1, dictionary)
  }


  "Zap rows" should "reset" in {
    TeslaWorkerCoupler {
      z1.initialize(builder, z1)
      z1.rowCount should equal(0)
      z1.navigate(builder, z1)
      z1.rowCount should equal(1)
      z1.initialize(builder, z1)
      z1.rowCount should equal(0)
    }
  }

  "Zap" should "should match during a navigation to same key" in {
    TeslaWorkerCoupler {
      z1.initialize(builder, z1)
      val r1 = z1.navigate(builder, z1, k1)
      val r2 = z1.navigate(builder, z1, k1)
      r1 should equal(r2)
      log info z1.printCube(builder, z1, dictionary)
    }
  }

  "Zap" should "should not match during a navigation to different key" in {
    TeslaWorkerCoupler {
      z1.initialize(builder, z1)
      val r1 = z1.navigate(builder, z1, k1)
      val r2 = z1.navigate(builder, z1, k2)
      z1.rowCount should equal(2)
      r1 should not equal r2

      z1.initialize(builder, z1)
      val r3 = z1.navigate(builder, z1, k1)
      val r4 = z1.navigate(builder, z1, k2)
      val r5 = z1.navigate(builder, z1, k3)
      z1.rowCount should equal(3)
      r3 should not equal r4
      r3 should not equal r5

      val r6 = z1.navigate(builder, z1, k3)
      r6 should equal(r5)
    }
  }

  "Zap" should "matching dimensions should have matching aggregation" in {
    TeslaWorkerCoupler {
      val t1 = 3333L
      val t2 = 6666L

      z1.initialize(builder, z1)
      val r1 = z1.navigate(builder, z1, k1)
      r1.writeRowAggregationPrimitive(builder, z1, 0, t1)
      r1.writeRowAggregationPrimitive(builder, z1, 1, t2)

      val r2 = z1.navigate(builder, z1, k1)
      r1.readRowAggregationPrimitive(builder, z1, 0) should equal(t1)
      r1.readRowAggregationPrimitive(builder, z1, 1) should equal(t2)
    }
  }

  // TODO
  /*
    "Zap" should "match a set of puts with gets" in {
      z1.reset(null)
      var i = 2
      while (i < 3) {
        val r0 = z1.navigate(d0.key)
        a0.set(r0)

        val r1 = z1.navigate(d1.key)
        a1.set(r1)

        val r2 = z1.navigate(d2.key)
        a2.set(r2)

        val t0 = z1.navigate(d0.key)
        a0.check(t0) should be(true)

        val t1 = z1.navigate(d1.key)
        a1.check(t1) should be(true)

        val t2 = z1.navigate(d2.key)
        a2.check(t2) should be(true)
        i += 1
      }

    }
  */


}
