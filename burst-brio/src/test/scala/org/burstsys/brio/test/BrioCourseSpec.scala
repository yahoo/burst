/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test

import org.burstsys.brio.types.BrioCourse._


class BrioCourseSpec extends BrioAbstractSpec {

  "Brio Course 32" should "calculate correctly" in {
    var c = BrioCourse32()

    c = c.mergeStep(0, 1)
    c = c.mergeStep(1, 2)
    c = c.mergeStep(2, 3)
    c = c.mergeStep(3, 0)
    c = c.mergeStep(4, 1)
    c = c.mergeStep(5, 2)
    c = c.mergeStep(6, 3)
    c = c.mergeStep(7, 0)
    c = c.mergeStep(8, 1)
    c = c.mergeStep(9, 2)
    c = c.mergeStep(10, 3)
    c = c.mergeStep(11, 0)

    c.toString should equal("1-2-3-1-2-3-1-2-3")

  }

  "Brio Course 16" should "calculate correctly" in {
    var c = BrioCourse16()

    c = c.mergeStep(0, 1)
    c = c.mergeStep(1, 2)
    c = c.mergeStep(2, 4)
    c = c.mergeStep(3, 0)
    c = c.mergeStep(4, 8)
    c = c.mergeStep(5, 12)
    c = c.mergeStep(6, 14)
    c = c.mergeStep(7, 0)
    c = c.mergeStep(8, 0)
    c = c.mergeStep(9, 0)
    c = c.mergeStep(10, 1)

    c.toString should equal("1-2-4-8-12-14-1")

  }

  "Brio Course 8" should "calculate correctly" in {
    var c = BrioCourse8()

    c = c.mergeStep(0, 25)
    c = c.mergeStep(1, 50)
    c = c.mergeStep(2, 150)
    c = c.mergeStep(3, 200)
    c = c.mergeStep(4, 100)
    c = c.mergeStep(5, 125)
    c = c.mergeStep(6, 150)
    c = c.mergeStep(7, 175)

    c.toString should equal("25-50-150-200-100-125-150-175")

  }

  "Brio Course 4" should "calculate correctly" in {
    var c = BrioCourse4()

    c = c.mergeStep(0, 100)
    c = c.mergeStep(1, 200)
    c = c.mergeStep(2, 300)
    c = c.mergeStep(3, 0)

    c.toString should equal("100-200-300")

  }


}
