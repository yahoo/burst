/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.truncate

import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec

class ZapCube2TruncateSpec extends ZapCube2Spec {

  val builder: ZapCube2Builder = cube2.ZapCube2Builder(dimensionCount = 2, aggregationCount = 2)

  private val k = 8

  private val aggValues = Seq.fill(5)(4L) ++
    Seq(10L) ++
    Seq.fill(3)(2L) ++
    Seq(11L) ++
    Seq.fill(2)(1L) ++
    Seq(12L) ++
    Seq.fill(3)(3L) ++
    Seq(13L) ++
    Seq.fill(3)(3L) ++
    Seq(14L)

  private val cubeRows = aggValues.indices.map(i => (i.toLong, 0L, aggValues(i), 0L))

  it should "truncate to bottom k " in {

    CubeTest {
      defineExplicitLong(cubeA, cubeRows: _*)
      cubeA.truncateToBottomKBasedOnAggregation(builder, cubeA, k, 0)

      cubeA.itemCount shouldEqual k
      val bottomK = for (i <- 0.until(cubeA.itemCount)) yield cubeA.row(i).aggRead(0)
      bottomK should contain theSameElementsInOrderAs Array(1, 1, 2, 2, 2, 3, 3, 3)
    }

  }
  it should "truncate to top k " in {

    CubeTest {
      defineExplicitLong(cubeA, cubeRows: _*)
      cubeA.truncateToTopKBasedOnAggregation(builder, cubeA, k, 0)

      cubeA.itemCount shouldEqual k
      val topK = for (i <- 0.until(cubeA.itemCount)) yield cubeA.row(i).aggRead(0)
      topK should contain theSameElementsInOrderAs Array(14, 13, 12, 11, 10, 4, 4, 4)
    }

  }


}
