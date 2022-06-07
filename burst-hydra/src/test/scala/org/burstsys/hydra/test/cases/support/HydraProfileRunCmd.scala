/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.support

import org.burstsys.fabric.metadata.model.over
import org.burstsys.hydra.runtime.{SerializeTraversal, StaticSweep}
import org.burstsys.vitals.errors.{VitalsException, messageFromException, safely}
import org.burstsys.vitals.uid.newBurstUid

import scala.concurrent.{Await, Promise}
import org.burstsys.tesla.thread.request._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait HydraProfileRunCmd {

  self: HydraUseCaseRunner =>

  def runProfileTest(useCase: HydraUseCase): Unit = {
    SerializeTraversal = useCase.serializeTraversal
    StaticSweep = useCase.sweep

    while (true) {
      test()
    }

    def test(): Unit = {
      val promise = Promise[Unit]()
      val analysis = useCase.analysisSource
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
  }
}
