/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.support

import org.burstsys.fabric.metadata.model.over
import org.burstsys.hydra.runtime.SerializeTraversal
import org.burstsys.hydra.runtime.StaticSweep
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors._
import org.burstsys.vitals.instrument.prettyPeriodString
import org.burstsys.vitals.instrument.prettyRateString
import org.burstsys.vitals.uid._

import scala.concurrent.Await
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success

trait HydraSerialRunCmd {

  self: HydraUseCaseRunner =>

  def runSerialTest(useCase: HydraUseCase): Unit = {
    SerializeTraversal = useCase.serializeTraversal
    StaticSweep = useCase.sweep

    def runUseCase(): Unit = {
      val promise = Promise[Unit]()
      val analysis = useCase.analysisSource
      log info s"\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\nUSECASE[${useCase.analysisName}]\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
      hydra.executeHydraAsWave(newBurstUid, analysis, over.FabricOver(domain = useCase.domain, view = useCase.view), parameters = None) onComplete {
        case Failure(t) => promise.failure(t)
        case Success(result) =>
          try {
            if (!result.resultStatus.isSuccess && !useCase.expectsException)
              throw VitalsException(s"execution failed: ${result.resultStatus}")
            if (result.groupMetrics.executionMetrics.overflowed > 0)
              throw VitalsException(s"execution overflowed")
            useCase.validate(result)
            promise.success((): Unit)
          } catch safely {
            case t: Throwable =>
              log error
                s"""
                   |--------------------------------------------------------------------------------------
                   |******* FAIL ********
                   |'${useCase.frameName}'
                   |${messageFromException(t)}
                   |${useCase.analysisSource}
                   |--------------------------------------""".stripMargin
              promise.failure(t)
          }
      }
      Await.result(promise.future, 5 minutes)
    }

    val start = System.nanoTime
    for (i <- 1 to useCase.executionCount) {
      runUseCase()
      log info
        s"""
           |--------------------------------------------------------------------------------------
           |******* RESULTS ********
           |$i execution(s)
           |${prettyRateString("execution", i, System.nanoTime - start)} ${prettyPeriodString("execution", i, System.nanoTime - start)}
           |--------------------------------------------------------------------------------------""".stripMargin

    }
  }
}
