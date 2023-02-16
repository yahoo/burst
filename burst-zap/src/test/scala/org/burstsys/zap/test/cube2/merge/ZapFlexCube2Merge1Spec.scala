/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.merge

import org.burstsys.brio.types.BrioTypes.BrioLongKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec

//@Ignore
class ZapFlexCube2Merge1Spec extends ZapCube2Spec {

  val builder: ZapCube2Builder = cube2.ZapCube2Builder(
    defaultStartSize = 1e6.toInt, // no resizing yet
    dimensionCount = 2, aggregationCount = 2,
    aggregationSemantics = Array(FeltCubeAggSumSemRt(), FeltCubeAggSumSemRt()),
    aggregationFieldTypes = Array(BrioLongKey, BrioLongKey),
    dimensionFieldTypes = Array(BrioLongKey, BrioLongKey)
  )

  it should "do a simple merge of non intersecting keys  " in {
    CubeTest {

      defineExplicitLong(cubeA,
        (0, 0, 0, 0),
        (1, 1, 1, 1),
        (2, 2, 2, 2),
        (3, 3, 3, 3)
      )

      defineExplicitLong(cubeB,
        (4, 4, 4, 4),
        (5, 5, 5, 5),
        (6, 6, 6, 6),
        (7, 7, 7, 7)
      )

      cubeA.interMerge(builder, cubeB)

      assertExplicitLong(cubeA,
        (0, 0, 0, 0),
        (1, 1, 1, 1),
        (2, 2, 2, 2),
        (3, 3, 3, 3),

        (4, 4, 4, 4),
        (5, 5, 5, 5),
        (6, 6, 6, 6),
        (7, 7, 7, 7)
      )

    }
  }
}
