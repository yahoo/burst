/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.join

import org.burstsys.brio.types.BrioTypes.BrioLongKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec
import org.scalatest.Ignore

//@Ignore
class ZapFlexCube2Join3Spec extends ZapCube2Spec {

  override val builder: ZapCube2Builder = cube2.ZapCube2Builder(
    defaultStartSize = 1e6.toInt, // no resizing yet
    dimensionCount = 2, aggregationCount = 2,
    aggregationSemantics = Array(FeltCubeAggSumSemRt(), FeltCubeAggSumSemRt()),
    aggregationFieldTypes = Array(BrioLongKey, BrioLongKey)
  )


  it should "do a simple join with upsizing -- separate parent/child dimensions/aggregations" in {

    CubeTest {

      // parent
      val segmentCount = 10
      defineAscending(cubeA, segmentCount)

      // define overlap of dimensions
      val overlapStart = segmentCount / 2
      val overlapEnd = overlapStart + segmentCount / 2

      defineAscending(cubeB, segmentCount, startIndex = overlapStart)


      // all dimensions and aggregations active
      val parentDimensionMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x0000001) // d0 from parent
      val parentAggregationMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000001) // a0 from parent
      val childDimensionMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000002) // d1 from child
      val childAggregationMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000002) // a1 from child

      cubeA.joinWithChildCubeIntoResultCube(
        builder, cubeA, dictA,
        childCube = cubeB, resultCube = cubeC,
        parentDimensionMask = parentDimensionMask, parentAggregationMask = parentAggregationMask,
        childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
      )

      assertTestLong(cubeC, segmentCount * segmentCount, {
        case (d0, d1, a0, a1) =>
          (d0, d1, d0, d1)
      })

    }
  }


}
