/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.join

import org.burstsys.brio.types.BrioTypes.BrioLongKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec

//@Ignore
class ZapFlexCube2Join1Spec extends ZapCube2Spec {

  override val builder: ZapCube2Builder = cube2.ZapCube2Builder(
    defaultStartSize = 10e6.toInt, // no resizing yet
    dimensionCount = 2, aggregationCount = 2,
    aggregationSemantics = Array(FeltCubeAggSumSemRt(), FeltCubeAggSumSemRt()),
    aggregationFieldTypes = Array(BrioLongKey, BrioLongKey)
  )

  it should "do a simple join with all different dimensions" in {

    CubeTest {

      // parent
      defineExplicitLong(cubeA,
        (1, 2, 111, 222),
        (3, 4, 333, 444)
      )

      // child
      defineExplicitLong(cubeB,
        (5, 6, 666, 666),
        (7, 8, 777, 888)
      )

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

      assertExplicitLong(cubeC,
        (1, 6, 111, 666),
        (3, 6, 333, 666),
        (1, 8, 111, 888),
        (3, 8, 333, 888)
      )

    }
  }

  it should "do a simple join with matching dimensions" in {

    CubeTest {

      // parent
      defineExplicitLong(cubeA,
        (1, 2, 111, 222),
        (3, 4, 333, 444)
      )

      // child
      defineExplicitLong(cubeB,
        (1, 2, 666, 666),
        (7, 8, 777, 888)
      )

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

      assertExplicitLong(cubeC,
        (1, 2, 111, 666),
        (3, 2, 333, 666),
        (1, 8, 111, 888),
        (3, 8, 333, 888)
      )

    }
  }

  it should "do a join with no child aggregations" in {

    CubeTest {

      // parent
      defineExplicitLong(cubeA,
        (1, 2, 111, 222),
        (3, 4, 333, 444)
      )

      // child
      defineExplicitLong(cubeB,
        (5, 6, 666, 666),
        (7, 8, 777, 888)
      )

      // all dimensions and aggregations active
      val parentDimensionMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x0000001) // d0 from parent
      val parentAggregationMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000003) // a0 from parent
      val childDimensionMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000002) // d1 from child
      val childAggregationMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000000) // a1 from child

      cubeA.joinWithChildCubeIntoResultCube(
        builder, cubeA, dictA,
        childCube = cubeB, resultCube = cubeC,
        parentDimensionMask = parentDimensionMask, parentAggregationMask = parentAggregationMask,
        childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
      )

      assertExplicitLong(cubeC,
        (1, 6, 111, 222),
        (1, 8, 111, 222),
        (3, 6, 333, 444),
        (3, 8, 333, 444)
      )

    }
  }
  it should "do a join with no parent aggregations" in {

    CubeTest {

      // parent
      defineExplicitLong(cubeA,
        (1, 2, 111, 222),
        (3, 4, 333, 444)
      )

      // child
      defineExplicitLong(cubeB,
        (5, 6, 666, 666),
        (7, 8, 777, 777)
      )

      // all dimensions and aggregations active
      val parentDimensionMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x0000001) // d0 from parent
      val parentAggregationMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000000) // a0 from parent
      val childDimensionMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000002) // d1 from child
      val childAggregationMask: VitalsBitMapAnyVal = VitalsBitMapAnyVal(0x00000003) // a1 from child

      cubeA.joinWithChildCubeIntoResultCube(
        builder, cubeA, dictA,
        childCube = cubeB, resultCube = cubeC,
        parentDimensionMask = parentDimensionMask, parentAggregationMask = parentAggregationMask,
        childDimensionMask = childDimensionMask, childAggregationMask = childAggregationMask
      )

      assertExplicitLong(cubeC,
        (1, 6, 666, 666),
        (3, 6, 666, 666),
        (1, 8, 777, 777),
        (3, 8, 777, 777)
      )

    }
  }

}
