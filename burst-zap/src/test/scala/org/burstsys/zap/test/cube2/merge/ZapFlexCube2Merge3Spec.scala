/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.merge

import org.burstsys.brio.types.BrioTypes.BrioLongKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec

//@Ignore
class ZapFlexCube2Merge3Spec extends ZapCube2Spec {

  val builder: ZapCube2Builder = cube2.ZapCube2Builder(
    defaultStartSize = 1000, // smallest size is pageSize so this really does not matter much
    dimensionCount = 2, aggregationCount = 2,
    aggregationSemantics = Array(FeltCubeAggSumSemRt(), FeltCubeAggSumSemRt()),
    dimensionFieldTypes = Array(BrioLongKey, BrioLongKey),
    aggregationFieldTypes = Array(BrioLongKey, BrioLongKey)
  )

  it should "do a simple merge of non intersecting keys for a large row set (growth)  " in {
    CubeTest {
      val count = 1e4.toInt // make it large to get over pageSize
      defineAscending(cubeA, count / 2, 0)
      defineAscending(cubeB, count / 2, count / 2)
      cubeA.interMerge(builder, cubeB)
      assertAscendingLong(cubeA, count, 0)
    }
  }

  it should "do a simple two stage merge of non intersecting keys for a large row set (growth)  " in {
    CubeTest {
      val count = 1e4.toInt // make it large to get over pageSize
      defineAscending(cubeA, count / 2, 0)
      defineAscending(cubeB, count / 2, count / 2)
      cubeA.interMerge(builder, cubeB)
      assertAscendingLong(cubeA, count, 0)
      defineAscending(cubeC, count / 2, count)
      cubeA.interMerge(builder, cubeC)
      assertAscendingLong(cubeA, count + count / 2, 0)
    }
  }

  it should "do a simple two stage merge of 100% intersecting keys for a large row set (no resize during merge)  " in {
    CubeTest {
      val count = 1e4.toInt // make it large to get over pageSize
      defineAscending(cubeA, count, 0)
      defineAscending(cubeB, count)
      cubeA.interMerge(builder, cubeB)
      assertTestLong(cubeA, count, {
        case (d0, d1, a0, a1) => (d0, d1, d0 * 2, d1 * 2)
      })
    }
  }

  it should "correctly resize during merge)  " in {
      CubeTest {
        val count = 1e4.toInt // make it large to get over pageSize
        defineAscending(cubeA, count, 0)
        defineAscending(cubeB, count - 10, startIndex = 10)
        cubeA.interMerge(builder, cubeB)
        assertTestLong(cubeA, count, {
          case (d0, d1, a0, a1) =>
            if (d0 < 10)
              (d0, d1, d0 * 1, d1 * 1)
            else
              (d0, d1, d0 * 2, d1 * 2)
        })
      }
  }

}
