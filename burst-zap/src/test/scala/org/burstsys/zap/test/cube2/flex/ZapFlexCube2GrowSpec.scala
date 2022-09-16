/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.flex

import org.burstsys.brio.dictionary.flex
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.io.log
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec

import scala.collection.mutable

//@Ignore
class ZapFlexCube2GrowSpec extends ZapCube2Spec {

  val builder: ZapCube2Builder = cube2.ZapCube2Builder(dimensionCount = 2, aggregationCount = 2)

  it should "grow enough rows to trigger an upsize event " in {

    val map = new mutable.HashMap[(Long, Long), (Long, Long)]

    TeslaWorkerCoupler {
      val dictionary = flex.grabFlexDictionary()
      val cube = cube2.flex.grabFlexCube(dictionary, builder = builder)

      var d0: Long = 0
      var d1: Long = 0
      var a0: Long = 0
      var a1: Long = 0
      try {
        val rowCount = 5000
        for (i <- 0 until rowCount) {
          map += (d0, d1) -> (a0, a1)
          cube.dimWrite(0, d0)
          cube.dimWrite(1, d1)
          cube.aggWrite(0, a0)
          cube.aggWrite(1, a1)
          log info s"ADD ROW($d0,$d1)($a0,$a1) valid=${cube.validate()}"
          cube.rowsCount should be(i+1)
          cube.validate() should be(true)
          d0 += 1
          d1 += 1
          a0 += 1
          a1 += 1
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

      } catch {
        case e: Throwable =>
          log error e
          throw e
      } finally {
        cube2.flex.releaseFlexCube(cube)
        flex.releaseFlexDictionary(dictionary)
      }
    }
  }


}
