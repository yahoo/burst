/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.bitmap

import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.vitals.test.VitalsAbstractSpec


class VitalsBitMapSpec extends VitalsAbstractSpec {

  "BurstBitMap" should "test upper byte" in {

    val foo: Long = 0xFFFF000000000000L

    val map = VitalsBitMapAnyVal(foo)
    for (i <- 0 to 47) {
      val test = map.testBit(i)
      test should equal(false)
    }

    for (i <- 48 to 63) {
      val test = map.testBit(i)
      test should equal(true)
    }

  }

  "BurstBitMap" should "test all zeros" in {

    val foo: Long = 0x0000000000000000L

    val map = VitalsBitMapAnyVal(foo)
    for (i <- 0 to 63) {
      val test = map.testBit(i)
      test should equal(false)
    }


  }


  "BurstBitMap" should "test all ones" in {

    val foo: Long = 0xFFFFFFFFFFFFFFFFL

    val map = VitalsBitMapAnyVal(foo)
    for (i <- 0 to 63) {
      val test = map.testBit(i)
      test should equal(true)
    }


  }

}
