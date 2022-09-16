/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.basic

import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary._
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.kryo.{acquireKryo, releaseKryo}
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec
import org.scalatest.Ignore

import scala.collection.mutable

//@Ignore
class ZapFlexCube2BasicSpec extends ZapCube2Spec {

  val builder: ZapCube2Builder = cube2.ZapCube2Builder(dimensionCount = 2, aggregationCount = 2)

  it should "grab/release a g2 flex cube" in {
    TeslaWorkerCoupler {
      val dictionary = flex.grabFlexDictionary()
      val cube = cube2.flex.grabFlexCube(dictionary, builder = builder)
      try {
        cube.rowsLimited should equal(false)
        cube.rowsCount should equal(0)
        cube.dimCount should equal(builder.dimensionCount)
        cube.aggCount should equal(builder.aggregationCount)
      } finally {
        cube2.flex.releaseFlexCube(cube)
        flex.releaseFlexDictionary(dictionary)
      }
    }
  }

  it should "add a row to a cube2" in {
    TeslaWorkerCoupler {
      val dictionary = flex.grabFlexDictionary()
      val cube = cube2.flex.grabFlexCube(dictionary, builder = builder)
      try {
        cube.dimWrite(0, 11)
        cube.dimWrite(1, 21)
        cube.aggWrite(0, 31)
        cube.aggWrite(1, 41)
        cube.rowsCount should equal(1)
      } finally {
        cube2.flex.releaseFlexCube(cube)
        flex.releaseFlexDictionary(dictionary)
      }
    }
  }

  it should "add a row to a cube2 with null aggregations and a dimWrite" in {
    TeslaWorkerCoupler {
      val dictionary = flex.grabFlexDictionary()
      val cube = cube2.flex.grabFlexCube(dictionary, builder = builder)
      try {
        cube.dimWrite(0, 1)
        cube.dimWrite(1, 2)
        cube.dimWrite()
        cube.rowsCount should equal(1)
      } finally {
        cube2.flex.releaseFlexCube(cube)
        flex.releaseFlexDictionary(dictionary)
      }
    }
  }

  it should "NOT add a row to a cube2 with null aggregations and no dimWrite" in {
    TeslaWorkerCoupler {
      val dictionary = flex.grabFlexDictionary()
      val cube = cube2.flex.grabFlexCube(dictionary, builder = builder)
      try {
        cube.dimWrite(0, 1)
        cube.dimWrite(1, 2)
        cube.rowsCount should equal(0)
      } finally {
        cube2.flex.releaseFlexCube(cube)
        flex.releaseFlexDictionary(dictionary)
      }
    }
  }

  it should "add two rows to a cube2 " in {
    TeslaWorkerCoupler {
      val dictionary = flex.grabFlexDictionary()
      val cube = cube2.flex.grabFlexCube(dictionary, builder = builder)
      try {
        cube.dimWrite(0, 1)
        cube.dimWrite(1, 2)
        cube.dimWrite()
        cube.dimWrite(0, 3)
        cube.dimWrite(1, 4)
        cube.dimWrite()
        cube.rowsCount should equal(2)
      } finally {
        cube2.flex.releaseFlexCube(cube)
        flex.releaseFlexDictionary(dictionary)
      }
    }
  }

  it should "add and retrieve many rows in a cube2 " in {

    val map = new mutable.HashMap[(Long, Long), (Long, Long)]

    TeslaWorkerCoupler {
      val dictionary = flex.grabFlexDictionary()
      val cube = cube2.flex.grabFlexCube(dictionary, builder = builder)

      var d0: Long = 10
      var d1: Long = 100
      var a0: Long = 1000
      var a1: Long = 10000
      try {
        val rowCount = 36
        for (i <- 0 until rowCount) {
          map += (d0, d1) -> (a0, a1)
          cube.dimWrite(0, d0)
          cube.dimWrite(1, d1)
          cube.aggWrite(0, a0)
          cube.aggWrite(1, a1)
          d0 += 1
          d1 += 3
          a0 += 5
          a1 += 7
        }
        cube.rowsCount should be(rowCount)

        map.size should equal(rowCount)

        var r = 0
        while (r < cube.rowsCount) {
          val row = cube.row(r)
          val key = Tuple2(row.dimRead(0), row.dimRead(1))
          val entry = map(key)
          entry should equal(row.aggRead(0), row.aggRead(1))
          map -= key
          r += 1
        }

        map.size should equal(0)

      } finally {
        cube2.flex.releaseFlexCube(cube)
        flex.releaseFlexDictionary(dictionary)
      }
    }
  }

  it should "serialize/deserialize " in {

    val map = new mutable.HashMap[(Long, Long), (Long, Long)]

    TeslaWorkerCoupler {
      val dictionary = flex.grabFlexDictionary()
      val cube = cube2.flex.grabFlexCube(dictionary, builder = builder)

      var d0: Long = 10
      var d1: Long = 100
      var a0: Long = 1000
      var a1: Long = 10000
      val rowCount = 36
      for (i <- 0 until rowCount) {
        map += (d0, d1) -> (a0, a1)
        cube.dimWrite(0, d0)
        cube.dimWrite(1, d1)
        cube.aggWrite(0, a0)
        cube.aggWrite(1, a1)
        d0 += 1
        d1 += 3
        a0 += 5
        a1 += 7
      }
      cube.rowsCount should be(rowCount)

      map.size should equal(rowCount)

      val destination = try {
        val k = acquireKryo
        try {
          val output = new Output(1e6.toInt)
          cube.write(k, output)
          val encoded = output.toBytes

          val tst = cube2.flex.grabFlexCube(dictionary, builder = builder)
          val input = new Input(encoded)
          tst.read(k, input)
          tst
        } finally {
          cube2.flex.releaseFlexCube(cube)
          releaseKryo(k)
        }
      } catch safely {
        case t: Throwable => throw VitalsException(t)
      }

      try {
        destination.rowsCount should be(rowCount)
        var r = 0
        while (r < destination.rowsCount) {
          val row = destination.row(r)
          val key = Tuple2(row.dimRead(0), row.dimRead(1))
          val entry = map(key)
          entry should equal(row.aggRead(0), row.aggRead(1))
          map -= key
          r += 1
        }

        map.size should equal(0)
      } finally {
        cube2.flex.releaseFlexCube(destination)
        flex.releaseFlexDictionary(dictionary)
      }

    }
  }

}
