/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.properties

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.burstsys.vitals
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.kryo.acquireKryo
import org.burstsys.vitals.kryo.releaseKryo
import org.burstsys.vitals.properties._
import org.burstsys.vitals.test.VitalsAbstractSpec

import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class VitalsPropertyMapSpec extends VitalsAbstractSpec {

  "VitalsPropertyMap" should "kryo export/import" in {
    val data: VitalsPropertyMap = Map(
      "foo" -> "56",
      "bar" -> "ff",
      "hello" -> "89"
    )

    val result = try {
      val k = acquireKryo
      try {
        val output = new Output(50000)
        vitals.properties.writePropertyMapToKryo(output, data)
        val encoded = output.toBytes

        val input = new Input(encoded)
        vitals.properties.readPropertyMapFromKryo(input)

      } finally releaseKryo(k)
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }

    result should equal(data)
  }

  it should "provide getValueOrThrow" in {
    val map = Map(
      "str" -> "bar",
      "num" -> "7",
      "bool" -> "true",
      "dur" -> "1 minute"
    )

    map.getValueOrThrow[String]("str") should equal("bar")
    map.getValueOrThrow[java.lang.String]("str") should equal("bar")

    map.getValueOrThrow[Int]("num") should equal(7)
    map.getValueOrThrow[Int]("num") shouldBe an[Int]
    map.getValueOrThrow[Long]("num") should equal(7L)
    map.getValueOrThrow[Long]("num") shouldBe a[Long]
    map.getValueOrThrow[Double]("num") should equal(7.0)
    map.getValueOrThrow[Double]("num") shouldBe a[Double]

    map.getValueOrThrow[Boolean]("bool") should equal(true)

    map.getValueOrThrow[Duration]("dur") should equal(1 minute)
  }

  it should "provide getValueOrDefault" in {
    val map: VitalsPropertyMap = Map()

    map.getValueOrDefault[String]("str", "bar") should equal("bar")
    map.getValueOrDefault[java.lang.String]("str", "bar") should equal("bar")

    map.getValueOrDefault[Int]("num", "7") should equal(7)
    map.getValueOrDefault[Long]("num", "7") should equal(7L)
    map.getValueOrDefault[Double]("num", "7") should equal(7.0)

    map.getValueOrDefault[Boolean]("bool", "true") should equal(true)

    map.getValueOrDefault[Duration]("dur", "1 minute") should equal(1 minute)
  }

  it should "convert to extended" in {
    val map = Map(
      "str" -> "bar"
      , "strAry" -> "{bar}"
      , "num" -> "7"
      , "numAry" -> "{7}"
      , "bool" -> "true"
      , "boolAry" -> "{true, false}"
      , "dur" -> "1 minute"
      , "durAry" -> "{1 minute, 2 minutes}"
    )

    val extended = map.extend
    val expected = Map(
      "str" -> "bar"
      , "strAry" -> Array("bar")
      , "num" -> 7
      , "numAry" -> Array(7)
      , "bool" -> true
      , "boolAry" -> Array(true, false)
      , "dur" -> (1 minute)
      , "durAry" -> Array(1 minute, 2 minutes)
    )
    expected foreach {
      case (k, v) => extended(k) should equal(v)
    }
  }

  "VitalsExtendedPropertyMap" should "have the same semantics as VitalsPropertyMap" in {
    val propMap = Map(
      "str" -> "bar",
      "num" -> "7",
      "bool" -> "true",
      "dur" -> "1 minute"
    )

    val extended = propMap.extend

    extended.getValueOrThrow[String]("str") should equal("bar")
    extended.getValueOrThrow[java.lang.String]("str") should equal("bar")

    extended.getValueOrThrow[Int]("num") should equal(7)
    extended.getValueOrThrow[Int]("num") shouldBe an[Int]
    extended.getValueOrThrow[Long]("num") should equal(7L)
    extended.getValueOrThrow[Long]("num") shouldBe a[Long]
    extended.getValueOrThrow[Double]("num") should equal(7.0)
    extended.getValueOrThrow[Double]("num") shouldBe a[Double]
    extended.getValueOrThrow[String]("num") should equal("7")
    extended.getValueOrThrow[String]("num") shouldBe a[String]

    extended.getValueOrThrow[Boolean]("bool") should equal(true)

    extended.getValueOrThrow[Duration]("dur") should equal(1 minute)
    extended.getValueOrThrow[String]("dur") should equal("1 minute")


    val emptyExtended: VitalsExtendedPropertyMap = Map()
    emptyExtended.getValueOrDefault[String]("str", "bar") should equal("bar")
    emptyExtended.getValueOrDefault[java.lang.String]("str", "bar") should equal("bar")

    emptyExtended.getValueOrDefault[Int]("num", 7) should equal(7)
    emptyExtended.getValueOrDefault[Int]("num", 7) shouldBe an[Int]
    emptyExtended.getValueOrDefault[Long]("num", 7L) should equal(7L)
    emptyExtended.getValueOrDefault[Long]("num", 7L) shouldBe a[Long]
    emptyExtended.getValueOrDefault[Double]("num", 7.0) should equal(7.0)
    emptyExtended.getValueOrDefault[Double]("num", 7.0) shouldBe a[Double]

    emptyExtended.getValueOrDefault[Boolean]("bool", true) should equal(true)

    emptyExtended.getValueOrDefault[Duration]("dur", 1 minute) should equal(1 minute)

  }

  it should "round trip from VitalsPropertyMap" in {
    val expected: VitalsExtendedPropertyMap = Map(
      "str" -> "bar"
      , "strAry" -> Array("bar")
      , "num" -> 7
      , "numAry" -> Array(7)
      , "bool" -> true
      , "boolAry" -> Array(true, false)
      , "dur" -> (1 minute)
      , "durAry" -> Array(1 minute, 2 minutes)
    )

    val extended = expected.restrict.extend
    expected foreach {
      case (k, v) => extended(k) should equal(v)
    }


    val expectedNestedArrays: VitalsExtendedPropertyMap = Map(
      "ints" -> Array[Int](1, 2, 3)
      , "longs" -> Array[Long](1L, 2L, 3L)
      , "dbls" -> Array[Double](1.0, 2.0, 3.0)
      , "strs" -> Array[String]("foo", "bar")
      , "bools" -> Array[Boolean](true, false)
      , "mixed" -> Array(true, "foo", 7.0, 7)
      // the next case is complicated for now. If we build arrays in #extend with StringTokenizer we can support this
      //, "wha" -> Array("str", Array(9, 4, false))
    )
    val nestedArrays = expectedNestedArrays.restrict.extend
    expectedNestedArrays foreach {
      case (k, v) => nestedArrays(k) should equal(v)
    }
  }

}
