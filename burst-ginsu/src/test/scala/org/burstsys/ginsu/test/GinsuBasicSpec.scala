/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.test

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.GinsuFunctions
import org.burstsys.ginsu.test.support.GinsuAbstractSpec

//@Ignore
class GinsuBasicSpec extends GinsuAbstractSpec {

  "burst memory block" should "initialize" in {

    implicit val thread: BrioThreadRuntime = new BrioThreadRuntime {}

    new GinsuFunctions {
      val test1 = Array(1.0, 5.0, 10.0)

      doubleSplitSlice(test1, -1.0).equals(Double.NaN) should be(true)
      doubleSplitSlice(test1, 0.0).equals(Double.NaN) should be(true)
      doubleSplitSlice(test1, 0.9).equals(Double.NaN) should be(true)
      doubleSplitSlice(test1, 1.0).equals(1.0) should be(true)
      doubleSplitSlice(test1, 5.0).equals(5.0) should be(true)
      doubleSplitSlice(test1, 9.9).equals(5.0) should be(true)
      doubleSplitSlice(test1, 10.0).equals(Double.NaN) should be(true)

      val test2 = Array(0.0, 5.0, 10.0)

      doubleSplitSlice(test2, -1.0).equals(Double.NaN) should be(true)
      doubleSplitSlice(test2, 0.0).equals(0.0) should be(true)
      doubleSplitSlice(test2, 0.9).equals(0.0) should be(true)
      doubleSplitSlice(test2, 1.0).equals(0.0) should be(true)
      doubleSplitSlice(test2, 4.999).equals(0.0) should be(true)
      doubleSplitSlice(test2, 5.0).equals(5.0) should be(true)

    }

  }


}
