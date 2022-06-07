/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.support

import org.burstsys.alloy.AlloyDatasetSpec
import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.usecase.AlloyJsonUseCaseRunner
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews
import org.burstsys.brio
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.hydra.HydraService
import org.burstsys.hydra.runtime.{SerializeTraversal, StaticSweep}
import org.burstsys.hydra.sweep.HydraSweep
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors._
import org.burstsys.vitals.instrument
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.burstsys.vitals.uid._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

abstract class HydraAlloyTestRunner extends AlloyJsonUseCaseRunner with VitalsLogger {

  override def localViews: Array[AlloyView] = super.localViews ++ Array(AlloyJsonUseCaseViews.quoSpecialView) ++ AlloyJsonUseCaseViews.quoViews

  final val AnalysisName = "myAnalysis"
  final val CubeFrame = "myCube"
  final val TabletFrame = "myTablet"
  final val RouteFrame = "myRoute"

  VitalsMetricsRegistry.disable()

  VitalsLog.configureLogging(logName = "hydra", consoleOnly = true)

  override def localStartup(): Unit = {
    log info s"Starting Hydra Alloy Test Runner"
    hydraService = HydraService(masterContainer).start
  }

  override def localShutdown(): Unit = {
    log info s"Stopping Hydra Alloy Test Runner"
    hydraService.stop
  }

  var hydraService: HydraService = _

  def test(hydra: String, ds: AlloyDatasetSpec,
           validate: (String, FabricResultSet) => Unit,
           parameters: String = "{}", staticSweep: Option[HydraSweep] = None,
           maxExecutions: Int = 1
          ): Unit = {
    if (staticSweep.nonEmpty) {
      SerializeTraversal = true
      StaticSweep = staticSweep.get
    }
    val over = FabricOver(domain = ds.domainKey, view = ds.viewKey)
    var executionCount = 0L
    var elapsedTime = 0L
    while (executionCount < maxExecutions) {
      executionCount += 1
      val start = System.nanoTime()
      execute(hydra, parameters, over, validate)
      val executeTime = System.nanoTime() - start
      elapsedTime += executeTime
      if (executionCount % 10 == 0) {
        val averageExecutionTime = elapsedTime / executionCount
        log info
          s"""|
              |----------------------------------------------------------------
              | executionCount=$executionCount,
              | averageExecutionTime=${instrument.prettyTimeFromNanos(averageExecutionTime)}
              | elapsedTime=${instrument.prettyTimeFromNanos(elapsedTime)}
              |----------------------------------------------------------------""".stripMargin
      }
    }
  }

  private def execute(hydra: String, parameters: String, over: FabricOver, validate: (String, FabricResultSet) => Unit): Unit = {
    val promise = Promise[FabricResultGroup]
    TeslaRequestCoupler {
      val source = hydra
      //        log info s"Hydra $over source:\n $source"
      hydraService.executeHydraAsWave(newBurstUid, source, over, Some(FabricCall(parameters))) onComplete {
        case Failure(t) =>
          promise.failure(t)
        case Success(result) =>
          Try {
            if (result.resultStatus.isFailure) {
              val msg = s"execution of hydra source '$source' ${result.resultStatus}"
              throw VitalsException(msg)
            }
            result.resultSetNames.foreach {
              case (frameName, frameId) =>
                val set = result.resultSets(frameId)
                validate(frameName, set)
            }
            result
          } match {
            case Failure(t) =>
              log error s"execution of hydra source \n'$source' \nFAIL[${messageFromException(t)}]"
              promise.failure(t)
            case Success(r) =>
              promise.success(r)
          }
      }
    }
    Await.result(promise.future, 10 minutes)
  }

  def assertLimits(r: FabricResultSet): Unit = {
    if (r.metrics.overflowed)
      throw VitalsException(s"frame ${r.resultName} overflowed")
    if (r.metrics.limited)
      throw VitalsException(s"frame ${r.resultName} row-limited")
  }


}
