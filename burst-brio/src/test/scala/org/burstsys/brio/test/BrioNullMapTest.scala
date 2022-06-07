/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test

import org.burstsys.brio.types.BrioNulls

class BrioNullMapTest extends BrioAbstractSpec {


  "Brio Alpha Null Maps mapWordCount" should "calculate correctly" in {
    var count = 0
    intercept[RuntimeException] {
      count = BrioNulls.mapLongCount(0.toByte)
    }

    count = BrioNulls.mapLongCount(1.toByte)
    count should be(1)

    count = BrioNulls.mapLongCount(7.toByte)
    count should be(1)

    count = BrioNulls.mapLongCount(8.toByte)
    count should be(1)

    count = BrioNulls.mapLongCount(9.toByte)
    count should be(1)

    count = BrioNulls.mapLongCount(63.toByte)
    count should be(1)

    count = BrioNulls.mapLongCount(64.toByte)
    count should be(2)

    count = BrioNulls.mapLongCount(65.toByte)
    count should be(2)

    count = BrioNulls.mapLongCount(90.toByte)
    count should be(2)

    count = BrioNulls.mapLongCount(126.toByte)
    count should be(2)

    count = BrioNulls.mapLongCount(127.toByte)
    count should be(2)

    intercept[RuntimeException] {
      count = BrioNulls.mapLongCount(128.toByte)
    }
    intercept[RuntimeException] {
      count = BrioNulls.mapLongCount(129.toByte)
    }
  }

  "Brio Alpha Null Maps locateBit" should "calculate correctly" in {

    BrioNulls.getWordOffset(0.toByte) should equal(0)
    BrioNulls.getIndexOffset(0.toByte) should equal(0)

    BrioNulls.getWordOffset(1.toByte) should equal(0)
    BrioNulls.getIndexOffset(1.toByte) should equal(1)

    BrioNulls.getWordOffset(63.toByte) should equal(0)
    BrioNulls.getIndexOffset(63.toByte) should equal(63)

    BrioNulls.getWordOffset(64.toByte) should equal(1)
    BrioNulls.getIndexOffset(64.toByte) should equal(0)

    BrioNulls.getWordOffset(65.toByte) should equal(1)
    BrioNulls.getIndexOffset(65.toByte) should equal(1)

    BrioNulls.getWordOffset(66.toByte) should equal(1)
    BrioNulls.getIndexOffset(66.toByte) should equal(2)

    BrioNulls.getWordOffset(126.toByte) should equal(1)
    BrioNulls.getIndexOffset(126.toByte) should equal(62)

    BrioNulls.getWordOffset(127.toByte) should equal(1)
    BrioNulls.getIndexOffset(127.toByte) should equal(63)

    intercept[RuntimeException] {
      BrioNulls.getWordOffset(128.toByte) should equal(1)
    }

    intercept[RuntimeException] {
      BrioNulls.getIndexOffset(128.toByte) should equal(1)
    }

  }


  "Brio Alpha Null Maps read and write bit" should "calculate correctly" in {
    val words = BrioNulls.mapLongCount(64.toByte)
    var map: Array[Long] = null

    map = new Array[Long](words)
    BrioNulls.setBit(0.toByte, map)
    BrioNulls.readBit(0.toByte, map) should equal(true)

    map = new Array[Long](words)
    BrioNulls.setBit(1.toByte, map)
    BrioNulls.readBit(1.toByte, map) should equal(true)

    map = new Array[Long](words)
    BrioNulls.setBit(63.toByte, map)
    BrioNulls.readBit(63.toByte, map) should equal(true)

    map = new Array[Long](words)
    BrioNulls.setBit(64.toByte, map)
    BrioNulls.readBit(64.toByte, map) should equal(true)

    map = new Array[Long](words)
    BrioNulls.setBit(65.toByte, map)
    BrioNulls.readBit(65.toByte, map) should equal(true)

    map = new Array[Long](words)
    BrioNulls.setBit(66.toByte, map)
    BrioNulls.readBit(66.toByte, map) should equal(true)

    map = new Array[Long](words)
    BrioNulls.setBit(125.toByte, map)
    BrioNulls.readBit(125.toByte, map) should equal(true)

    map = new Array[Long](words)
    BrioNulls.setBit(126.toByte, map)
    BrioNulls.readBit(126.toByte, map) should equal(true)

    map = new Array[Long](words)
    BrioNulls.setBit(127.toByte, map)
    BrioNulls.readBit(127.toByte, map) should equal(true)
  }

}
