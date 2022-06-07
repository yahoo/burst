/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.verbatim.FeltCubeDimVerbatimSemRt
import org.burstsys.felt.model.collectors.cube.runtime.{FeltCubeOrdinalMap, FeltCubeTreeMask}
import org.burstsys.tesla.block.TeslaBlock
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.zap.cube.{ZapCubeBuilder, ZapCubeContext, ZapCubeRow}
import org.burstsys.zap.test.cube.ZapPerformanceTest.rows
import org.burstsys.{brio, tesla}

object ZapTestData {

  implicit var z1: ZapCubeContext = _

  val builder: ZapCubeBuilder = ZapCubeBuilder(
    // rows
    rows,

    Array("Field1", "Field2"),

    // dimensions
    2,
    Array(FeltCubeDimVerbatimSemRt(), FeltCubeDimVerbatimSemRt()),
    Array(BrioLongKey, BrioLongKey),
    FeltCubeOrdinalMap(),
    FeltCubeTreeMask(),

    // aggregations
    2,
    Array(FeltCubeAggSumSemRt(), FeltCubeAggSumSemRt()),
    Array(BrioLongKey, BrioLongKey),
    FeltCubeOrdinalMap(),
    FeltCubeTreeMask()
  )

  case class TestDimension(d0: Long, d1: Long) {
    def key: FabricDataKeyAnyVal = {
      val k = FabricDataKeyAnyVal(new Array[Long](3))
      k.writeKeyDimensionPrimitive(0, d0)
      k.writeKeyDimensionPrimitive(1, d1)
      k
    }
  }

  case class TestAggregation(d0: Long, d1: Long) {
    def set(row: ZapCubeRow): Unit = {
      row.writeRowAggregationPrimitive(builder, z1, 0, d0)
      row.writeRowAggregationPrimitive(builder, z1, 1, d1)
    }

    def check(row: ZapCubeRow): Boolean = {
      row.readRowAggregationPrimitive(builder, z1, 0) == d0 && row.readRowAggregationPrimitive(builder, z1, 1) == d1
    }
  }

  case class TestRow(dimension: TestDimension, aggregations: TestAggregation)

  val d0 = TestDimension(1234L, 2345L)
  val a0 = TestAggregation(3456L, 4567L)
  val d1 = TestDimension(5678L, 6789L)
  val a1 = TestAggregation(7890L, 8901L)
  val d2 = TestDimension(9012L, 123L)
  val a2 = TestAggregation(2345L, 3456L)

  val k1 = FabricDataKeyAnyVal(new Array[Long](3))
  val k2 = FabricDataKeyAnyVal(new Array[Long](3))
  val k3 = FabricDataKeyAnyVal(new Array[Long](3))
  val k4 = FabricDataKeyAnyVal(new Array[Long](4))

  implicit val s: ZapCubeBuilder = ZapCubeBuilder(
    // rows
    512,

    Array("dim1", "dim2", "agg1", "agg2"),

    // dimensions
    2,
    Array(FeltCubeDimVerbatimSemRt(), FeltCubeDimVerbatimSemRt()),
    Array(BrioLongKey, BrioLongKey),
    FeltCubeOrdinalMap(),
    FeltCubeTreeMask(),

    // aggregations
    2,
    Array(FeltCubeAggSumSemRt(), FeltCubeAggSumSemRt()),
    Array(BrioLongKey, BrioLongKey),
    FeltCubeOrdinalMap(),
    FeltCubeTreeMask()
  )

  val testRows: Array[TestRow] = Array(TestRow(d0, a0), TestRow(d1, a1), TestRow(d2, a2))

  def initData(): Unit = {
    TeslaWorkerCoupler {

      val block: TeslaBlock = tesla.block.factory.grabBlock(s.totalMemorySize)
      val dictionary = brio.dictionary.factory.grabMutableDictionary()
      z1 = new ZapCubeContext(block.blockBasePtr, -1)
      z1.initialize(builder, z1)
      k1.writeKeyDimensionPrimitive(0, 2L)
      k1.writeKeyDimensionPrimitive(1, 3L)

      k2.writeKeyDimensionPrimitive(0, 2L)


      k4.writeKeyDimensionPrimitive(0, 22L)
      k4.writeKeyDimensionPrimitive(1, 33L)
      k4.writeKeyDimensionPrimitive(2, 66L)

    }

  }

}
