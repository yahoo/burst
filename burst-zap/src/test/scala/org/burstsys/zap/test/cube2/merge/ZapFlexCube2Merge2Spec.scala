/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.merge

import org.burstsys.brio.types.BrioTypes.BrioLongKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec

//@Ignore
class ZapFlexCube2Merge2Spec extends ZapCube2Spec {

  val builder: ZapCube2Builder = cube2.ZapCube2Builder(
    defaultStartSize = 1e6.toInt, // no resizing yet
    dimensionCount = 2, aggregationCount = 2,
    aggregationSemantics = Array(FeltCubeAggSumSemRt(), FeltCubeAggSumSemRt()),
    dimensionFieldTypes = Array(BrioLongKey, BrioLongKey),
    aggregationFieldTypes = Array(BrioLongKey, BrioLongKey)
  )

  it should "do a simple merge of intersecting keys  " in {
    CubeTest {

      defineExplicitLong(cubeA,
        (0, 0, 0, 0),
        (1, 1, 1, 1),
        (2, 2, 2, 2), // intersect
        (3, 3, 3, 3) // intersect
      )

      defineExplicitLong(cubeB,
        (2, 2, 2, 2), // intersect
        (3, 3, 3, 3), // intersect
        (4, 4, 4, 4),
        (5, 5, 5, 5)
      )

      cubeA.interMerge(builder, cubeB)


      assertExplicitLong(cubeA,
        (0, 0, 0, 0),
        (1, 1, 1, 1),
        (2, 2, 4, 4), // intersect
        (3, 3, 6, 6), // intersect
        (4, 4, 4, 4),
        (5, 5, 5, 5)
      )

    }
  }
}
