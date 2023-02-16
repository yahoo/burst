/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.merge

import org.burstsys.brio.types.BrioTypes.{BrioLongKey, BrioStringKey}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec

//@Ignore
class ZapFlexCube2Merge4Spec extends ZapCube2Spec {

  val builder: ZapCube2Builder = cube2.ZapCube2Builder(
    defaultStartSize = 1000, // smallest size is pageSize so this really does not matter much
    dimensionCount = 1, aggregationCount = 1,
    dimensionFieldTypes = Array(BrioStringKey),
    aggregationSemantics = Array(FeltCubeAggSumSemRt()),
    aggregationFieldTypes = Array(BrioLongKey)
  )

  it should "do a simple merge of non intersecting keys for a large row set (growth)  " in {
    CubeTest {
      val count = 1e4.toInt // make it large to get over pageSize
      for (i <- 0 until count) {
        val d = cubeA.dictionary
        cubeA.dimWrite(0, d.keyLookupWithAdd(s"user$i"))
        cubeA.aggWrite(0, 1)
      }
      for (i <- count until 2*count) {
        val d = cubeB.dictionary
        cubeB.dimWrite(0, d.keyLookupWithAdd(s"user$i"))
        cubeB.aggWrite(0, 1)
      }
      cubeA.interMerge(builder, cubeB)
      val cubeADict = cubeA.dictionary
      val rez: Array[Tuple2[String, Int]] = (for (i <- 0 until cubeA.rowsCount) yield {
        val r = cubeA.row(i)
        val a = r.aggRead(0).toInt
        val d = cubeADict.stringLookup(r.dimRead(0).toShort)
        (d, a)
      }).toArray
      rez.length should equal (2*count)
      rez.sortInPlaceBy(t => t._1.drop(4).toInt)
      for (i <- 0 until 2*count) {
        rez(i)._2 should equal(1)
        rez(i)._1 should equal (s"user$i")
      }
    }
  }

  it should "do a simple merge of intersecting keys for a large row set (growth)  " in {
    CubeTest {
      val count = 1e4.toInt // make it large to get over pageSize
      for (i <- 0 until count) {
        val d = cubeA.dictionary
        cubeA.dimWrite(0, d.keyLookupWithAdd(s"user$i"))
        cubeA.aggWrite(0, 1)
      }
      for (i <- 0 until count) {
        val d = cubeB.dictionary
        cubeB.dimWrite(0, d.keyLookupWithAdd(s"user${2*i}"))
        cubeB.aggWrite(0, 1)
      }
      cubeA.interMerge(builder, cubeB)
      val cubeADict = cubeA.dictionary
      val rez: Array[Tuple2[String, Int]] = (for (i <- 0 until cubeA.rowsCount) yield {
        val r = cubeA.row(i)
        val a = r.aggRead(0).toInt
        val d = cubeADict.stringLookup(r.dimRead(0).toShort)
        (d, a)
      }).toArray
      rez.length should equal(count + count/2)
      rez.sortInPlaceBy(t => t._1.drop(4).toInt)
      for (i <- 0 until (count + count/2)) {
        // in the first <count> element the evens will aggregage to 2
        if (i < count && i%2 == 0) {
          rez(i)._2 should equal(2)
          rez(i)._1 should equal(s"user$i")
        } else if (i < count) {
          rez(i)._2 should equal(1)
          rez(i)._1 should equal(s"user$i")
        } else if (i >= count) {
          rez(i)._2 should equal(1)
          rez(i)._1 should equal(s"user${(i-(count/2))*2}")
        }
      }
    }
  }

}
