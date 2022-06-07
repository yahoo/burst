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

  private val sequence = Array[BrioPrimitive](
    3, 700, 12, 45, 100, 500, 300, 400, 200, 43, 600, 2, 999, 23, 404, 505,
    77, 2000, 349, 1277, 4596, 7888, 33334, 2220, 222228, 1022
  )


  "Zap Topper" should "do a simple top k sort/truncate" in {

    initData()
    TeslaWorkerCoupler {

      val dictionary = brio.dictionary.factory.grabMutableDictionary()
      try {

        // populate the map with a bunch of rows
        z1.initialize(builder = builder, thisCube = z1)
        z1.rowCount should equal(0)

        for (i <- sequence.indices) {
          val key = FabricDataKeyAnyVal(Array(0L, 0L, 0L))
          key.writeKeyDimensionPrimitive(0, i)
          val row = z1.navigate(builder, z1, key)
          row.writeRowAggregationPrimitive(builder, z1, 0, sequence(i))

        }

        z1.truncateToTopKBasedOnAggregation(builder, z1, 5, 0)

        val topK = new ArrayBuffer[BrioPrimitive]

        z1.foreachRow(builder, z1, {
          row => topK += row.readRowAggregationPrimitive(builder, z1, 0)
        })

        topK.sorted should equal(Array(2220, 4596, 7888, 33334, 222228))


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

        for (i <- sequence.indices) {
          val key = FabricDataKeyAnyVal(Array(0L, 0L, 0L))
          key.writeKeyDimensionPrimitive(0, i)
          val row = z1.navigate(builder, z1, key)
          row.writeRowAggregationPrimitive(builder, z1, 0, sequence(i))

        }

        z1.truncateToBottomKBasedOnAggregation(builder, z1, 5, 0)

        val topK = new ArrayBuffer[BrioPrimitive]

        z1.foreachRow(builder, z1, {
          row => topK += row.readRowAggregationPrimitive(builder, z1, 0)
        })

        topK.sorted should equal(Array(2, 3, 12, 23, 43))

      } finally {
        brio.dictionary.factory.releaseMutableDictionary(dictionary)
      }
    }
  }


}
