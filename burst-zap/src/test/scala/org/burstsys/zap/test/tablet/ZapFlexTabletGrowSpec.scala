/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.tablet

import org.burstsys.tesla.TeslaTypes.SizeOfLong
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.zap.tablet.ZapTabletBuilder
import org.burstsys.zap.tablet.flex.ZapFlexTablet
import org.burstsys.zap.test.ZapAbstractSpec
import org.burstsys.zap.{route, tablet}

class ZapFlexTabletGrowSpec extends ZapAbstractSpec {

  val builder: ZapTabletBuilder = tablet.ZapTabletBuilder(SizeOfLong)

  it should "grow enough step assertions to trigger an upsize event " in {

    TeslaWorkerCoupler {
      val testTablet: ZapFlexTablet = tablet.flex.grabFlexTablet(builder, route.ZapRouteDefaultStartSize)
      try {
        val itemCount = 100000
        for (i <- 0 until itemCount) {
          testTablet.tabletAddLong(i)
        }

        testTablet.itemCount should equal(itemCount)
      } catch {
        case e: Throwable =>
          log error e
          throw e
      } finally {
        tablet.flex.releaseFlexTablet(testTablet)
      }
    }
  }

}
