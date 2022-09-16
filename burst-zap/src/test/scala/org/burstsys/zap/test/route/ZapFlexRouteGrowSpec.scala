/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.route

import org.burstsys.felt.model.collectors.route.decl.graph.{FeltRouteEdge, FeltRouteTransition}
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.zap.route
import org.burstsys.zap.route.ZapRouteBuilder
import org.burstsys.zap.route.flex.ZapFlexRoute
import org.burstsys.zap.test.ZapAbstractSpec

//@Ignore
class ZapFlexRouteGrowSpec extends ZapAbstractSpec {

  val builder: ZapRouteBuilder = route.ZapRouteBuilder(
    maxPartialPaths = 100000000,
    maxCompletePaths = -1,
    maxStepsPerGraph = 100000000,
    maxPathTime = -1,
    minCourse = -1,
    maxCourse = -1,
    entranceSteps = Array(1),
    exitSteps = Array(2),
    beginSteps = Array.empty,
    tacitSteps = Array.empty,
    emitCodes = Array(0, 0),
    endSteps = Array.empty,
    completeSteps = Array.empty,
    transitions = Array(FeltRouteTransition(Array(FeltRouteEdge(stepKey = 2, maxTime = 0, minTime = 0))))
  )

  it should "grow enough step assertions to trigger an upsize event " in {

    TeslaWorkerCoupler {
      val testRoute: ZapFlexRoute = route.flex.grabFlexRoute(builder, route.ZapRouteDefaultStartSize)

      try {
        val stepCount = 100000
        for (i <- 0 until stepCount) {
          testRoute.routeScopeStart(builder)
          testRoute.routeFsmStepAssert(builder, 1, 1, i)
          testRoute.routeFsmStepAssert(builder, 2, 2, i)
          testRoute.routeScopeCommit(builder)
          testRoute.validate() should equal(true)
        }

        testRoute.itemCount should equal(stepCount * 2)

      } catch {
        case e: Throwable =>
          log error e
          throw e
      } finally {
        route.flex.releaseFlexRoute(testRoute)
      }
    }
  }

}
