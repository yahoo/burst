/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.join

import org.burstsys.brio.types.BrioTypes.BrioLongKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec
import org.scalatest.Ignore

@Ignore
class ZapFlexCube2Join2Spec extends ZapCube2Spec {

  override val builder: ZapCube2Builder = cube2.ZapCube2Builder(
    defaultStartSize = 1e3.toInt, // no resizing yet
    dimensionCount = 2, aggregationCount = 2,
    aggregationSemantics = Array(FeltCubeAggSumSemRt(), FeltCubeAggSumSemRt()),
    aggregationFieldTypes = Array(BrioLongKey, BrioLongKey)
  )


  it should "do a simple join with upsizing" in {

    CubeTest {

      // parent
      defineAscending(cubeA, 50)

      // child
      defineAscending(cubeB, 50, startIndex = 50)

      log info s"JOIN"

      // all dimensions and aggregations active
      val parentDimensionMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x0000001) // d0 from parent
      val parentAggregationMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000001) // a0 from parent
      val childDimensionMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000002) // d1 from child
      val childAggregationMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000002) // a1 from child

      val f1 = cubeA.toString

      val f2 = cubeB.toString

      cubeA.joinWithChildCubeIntoResultCube(
        builder, cubeA, dictA,
        childCube = cubeB, resultCube = cubeC,
        parentDimensionMask = parentDimensionMask, parentAggregationMask = parentAggregationMask,
        childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
      )

      val f3 = cubeC.toString

      assertExplicitLong(cubeC,
        (1, 2, 111, 666),
        (3, 2, 333, 666),
        (1, 8, 111, 888),
        (3, 8, 333, 888)
      )

    }
  }


}
