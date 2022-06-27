/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube

import org.burstsys.brio
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.zap.test.ZapAbstractSpec

import scala.collection.mutable.ArrayBuffer

//@Ignore
class ZapTopperSpec extends ZapAbstractSpec {

  import ZapTestData._

  private val values = Seq.fill(5)(4: BrioPrimitive) ++
    Seq(10: BrioPrimitive) ++
    Seq.fill(3)(2: BrioPrimitive) ++
    Seq(11: BrioPrimitive) ++
    Seq.fill(2)(1: BrioPrimitive) ++
    Seq(12: BrioPrimitive) ++
    Seq.fill(3)(3: BrioPrimitive) ++
    Seq(13: BrioPrimitive) ++
    Seq.fill(3)(3: BrioPrimitive) ++
    Seq(14: BrioPrimitive)


  "Zap Topper" should "do a simple top k sort/truncate" in {
    initData()
    TeslaWorkerCoupler {

      val dictionary = brio.dictionary.factory.grabMutableDictionary()
      try {

        // populate the map with a bunch of rows
        z1.initialize(builder = builder, thisCube = z1)
        z1.rowCount should equal(0)

        for (i <- values.indices) {
          val key = FabricDataKeyAnyVal(Array(0L, 0L, 0L))
          key.writeKeyDimensionPrimitive(0, i)
          val row = z1.navigate(builder, z1, key)
          row.writeRowAggregationPrimitive(builder, z1, 0, values(i))
        }

        z1.truncateToTopKBasedOnAggregation(builder, z1, 8, 0)

        val topK = new ArrayBuffer[BrioPrimitive]

        z1.foreachRow(builder, z1, {
          row => topK += row.readRowAggregationPrimitive(builder, z1, 0)
        })

        topK should contain theSameElementsInOrderAs Array(14, 13, 12, 11, 10, 4, 4, 4)


      } finally {
        brio.dictionary.factory.releaseMutableDictionary(dictionary)
      }
    }
  }

  "Zap Topper" should "do a simple bottom k sort/truncate" in {
    TeslaWorkerCoupler {

      val dictionary = brio.dictionary.factory.grabMutableDictionary()
      try {

        // populate the map with a bunch of rows
        z1.initialize(builder, z1)
        z1.rowCount should equal(0)

        for (i <- values.indices) {
          val key = FabricDataKeyAnyVal(Array(0L, 0L, 0L))
          key.writeKeyDimensionPrimitive(0, i)
          val row = z1.navigate(builder, z1, key)
          row.writeRowAggregationPrimitive(builder, z1, 0, values(i))

        }

        z1.truncateToBottomKBasedOnAggregation(builder, z1, 8, 0)

        val topK = new ArrayBuffer[BrioPrimitive]

        z1.foreachRow(builder, z1, {
          row => topK += row.readRowAggregationPrimitive(builder, z1, 0)
        })

        topK should contain theSameElementsInOrderAs Array(1, 1, 2, 2, 2, 3, 3, 3)

      } finally {
        brio.dictionary.factory.releaseMutableDictionary(dictionary)
      }
    }
  }


}
