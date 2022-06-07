/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.reporter

import org.burstsys.vitals.host.{VitalsGcReporter, VitalsHostReporter}
import org.burstsys.vitals.test.VitalsAbstractSpec
import org.scalatest.Ignore

import scala.language.postfixOps


@Ignore
class VitalsHostStateSpec extends VitalsAbstractSpec {

  it should "test vitals host state" in {

    VitalsHostReporter.start()
    VitalsGcReporter.start()

    var foo = 0
    for (i <- 0 until 1e7.toInt) {
      for (j <- 0 until 1e8.toInt) {
        new Object()
      }
      if (foo > 30) {
        foo = 0
        System.gc()
      } else
        foo += 1

      Thread.sleep(100)
    }


  }

}
