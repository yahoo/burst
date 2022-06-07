/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.truncate

import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec

//@Ignore
class ZapCube2TruncateSpec extends ZapCube2Spec {

  val builder: ZapCube2Builder = cube2.ZapCube2Builder(dimensionCount = 2, aggregationCount = 2)

  it should "truncate to bottom k " in {

    CubeTest {
      defineAscending(cubeA, 100)
      cubeA.truncateToBottomKBasedOnAggregation(builder, cubeA, 5, 0)

      assertExplicitLong(cubeA,
        (0, 0, 0, 0),
        (1, 1, 1, 1),
        (2, 2, 2, 2),
        (3, 3, 3, 3),
        (4, 4, 4, 4)
      )

    }

  }
  it should "truncate to top k " in {

    CubeTest {
      defineAscending(cubeA, 100)
      cubeA.truncateToTopKBasedOnAggregation(builder, cubeA, 5, 0)

      assertExplicitLong(cubeA,
        (95, 95, 95, 95),
        (96, 96, 96, 96),
        (97, 97, 97, 97),
        (98, 98, 98, 98),
        (99, 99, 99, 99)
      )

    }

  }


}
