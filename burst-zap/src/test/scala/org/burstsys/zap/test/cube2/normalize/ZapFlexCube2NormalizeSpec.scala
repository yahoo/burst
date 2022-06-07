/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.normalize

import org.burstsys.brio.types.BrioTypes.BrioStringKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.cube2.flex.ZapFlexCube2
import org.burstsys.zap.test.cube2.ZapCube2Spec
import org.scalatest.Ignore

//@Ignore
class ZapFlexCube2NormalizeSpec extends ZapCube2Spec {

  override val builder: ZapCube2Builder = cube2.ZapCube2Builder(
    defaultStartSize = 10e6.toInt, // no resizing yet
    dimensionCount = 2, aggregationCount = 2,
    aggregationSemantics = Array(FeltCubeAggSumSemRt(), FeltCubeAggSumSemRt()),
    dimensionFieldTypes = Array(BrioStringKey, BrioStringKey),
    aggregationFieldTypes = Array(BrioStringKey, BrioStringKey)
  )

  it should "do a simple normalize" in {

    CubeTest {

      // this
      defineExplicitString(cubeA,
        ("11", "22", "33", "44"),
        ("55", "66", "77", "88")
      )

      // that
      defineExplicitString(cubeB,
        ("1", "2", "3", "4"),
        ("5", "6", "7", "8")
      )

      cubeA.dictionary should not equal cubeB.dictionary

      cubeB = cubeA.normalizeThatCubeToThis(cubeB, builder, text).asInstanceOf[ZapFlexCube2]

      assertExplicitString(cubeB,
        ("1", "2", "3", "4"),
        ("5", "6", "7", "8")
      )

    }
  }

}
