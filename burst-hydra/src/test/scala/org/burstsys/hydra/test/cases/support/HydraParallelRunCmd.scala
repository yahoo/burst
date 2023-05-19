/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.support

import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.metadata.model.over
import org.burstsys.hydra.runtime.{SerializeTraversal, StaticSweep}
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.{VitalsException, messageFromException, _}
import org.burstsys.vitals.test.VitalsTest.parallelExecute
import org.burstsys.vitals.uid._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait HydraParallelRunCmd {

  self: HydraUseCaseRunner =>

  def runParallelTest(useCase: HydraUseCase, concurrency: Int = 1): Unit = {

    SerializeTraversal = useCase.serializeTraversal
    StaticSweep = useCase.sweep

    parallelExecute(useCase.analysisName, "query", concurrency, useCase.timeout, {
      val promise = Promise[Unit]()
      val analysis = useCase.analysisSource
      log info s"\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\nUSECASE[${useCase.analysisName}]\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
      val parameters = if (useCase.parameters == null) None else Some(FabricCall(useCase.parameters))
      hydra.executeHydraAsWave(newBurstUid, analysis, over.FabricOver(domain = useCase.domain, view = useCase.view), parameters) onComplete {
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
              log error(
                s"""
                   |FAILURE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                   |-----------------------------------------
                   |'${useCase.frameName}'
                   |
                   |${messageFromException(t)}
                   |
                   |${useCase.analysisSource}
                   |
                   |-----------------------------------------
                 """.stripMargin, t)
              promise.failure(t)
          }
      }
      Await.result(promise.future, 5 minutes)
    })
  }

}
