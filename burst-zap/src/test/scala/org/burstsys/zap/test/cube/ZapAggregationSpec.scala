/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube

import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggUniqueSemRt
import org.burstsys.zap.test.ZapAbstractSpec

//@Ignore
class ZapAggregationSpec extends ZapAbstractSpec {


  "Zap Aggregations" should "initialize correctly" in {

    FeltCubeAggUniqueSemRt().doLong(10, 10, intra = true) should equal(1)
    FeltCubeAggUniqueSemRt().doLong(1, 1, intra = true) should equal(1)
    FeltCubeAggUniqueSemRt().doLong(1, 1, intra = false) should equal(2)
  }


}
