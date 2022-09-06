/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube

import java.util

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.verbatim.FeltCubeDimVerbatimSemRt
import org.burstsys.felt.model.collectors.cube.runtime.{FeltCubeOrdinalMap, FeltCubeTreeMask}
import org.burstsys.zap.cube.{ZapCubeBuilder, ZapCubeContext}
import org.burstsys.zap.test._
import org.burstsys.zap.test.cube.ZapTestData.builder
import org.burstsys.{brio, tesla}

object ZapPerformanceTest extends ZapAbstractSpec {

  final val rows: BrioRelationCount = 1024

  def main(args: Array[String]): Unit = {
    val iterations = 1e6.toInt
    val keys = rows

    val d = brio.dictionary.factory.grabMutableDictionary()
    val block = tesla.block.factory.grabBlock(builder.totalMemorySize)

    import org.burstsys.vitals.instrument._
    if (true) {
      val z1: ZapCubeContext = new ZapCubeContext(block.blockBasePtr, -1)
      z1.initialize(builder, z1)
      var t = 0
      var value: Long = 31
      var start = System.nanoTime
      while (t < iterations) {
        var i = 0
        z1.initialize(builder, z1)
        while (i < keys) {
          val k = FabricDataKeyAnyVal(new Array[Long](3))
          k.writeKeyDimensionPrimitive(0, value)
          value += value << 7
          k.writeKeyDimensionPrimitive(1, value)
          value += value << 9
          z1.navigate(builder, z1, k)
          i += 1
        }
        t += 1
      }
      ZapPerformanceTest.log info s"ZapCube Total key(s) inserted: ${
        displayCountAverage(rows * iterations, "key", System.nanoTime - start)
      } "
    }

    if (false) {
      val h1 = new util.HashMap[DimKey, Long]
      val start = System.nanoTime
      var value: Long = 31
      var t = 0
      while (t < iterations) {
        h1.clear()
        var i = 0
        while (i < keys) {
          val v1 = value
          value += value << 7
          val v2 = value
          value += value << 9
          h1.put(new DimKey(v1, v2), 0L)
          i += 1
        }
        t += 1
      }
      ZapPerformanceTest.log info s"Hashmap key(s) inserted: ${
        displayCountAverage(rows * iterations, "key", System.nanoTime - start)
      } "
    }


  }

  class DimKey(val v1: Long, val v2: Long) {
    override def hashCode(): Int = v1.hashCode * (v2.hashCode * 31)

    override def equals(obj: scala.Any): Boolean = {
      val k = obj.asInstanceOf[DimKey]
      v1 == k.v1 && v2 == k.v2
    }
  }

}
