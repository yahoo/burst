/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.support

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.usecase.AlloyJsonUseCaseRunner
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews
import org.burstsys.hydra.HydraService
import org.burstsys.vitals.logging._

import scala.language.postfixOps

abstract class HydraUseCaseRunner extends AlloyJsonUseCaseRunner
  with HydraParallelRunCmd with HydraSerialRunCmd with HydraProfileRunCmd {

  override def localViews: Array[AlloyView] = super.localViews ++ Array(AlloyJsonUseCaseViews.quoSpecialView) ++ AlloyJsonUseCaseViews.quoViews

  VitalsLog.configureLogging("unit", consoleOnly = true)

  var hydra: HydraService = _


  override def localStartup(): Unit = {
    hydra = HydraService(masterContainer).start
  }

  override def localShutdown(): Unit = {
    hydra.stop
  }
}


abstract class HydraUseCaseSpec(useCases: Seq[HydraUseCase], schema: String = "unknown") extends HydraUseCaseRunner {
  useCases.foreach { useCase =>
    it should s"run $schema use case ${useCase.analysisName}" in {
      runSerialTest(useCase)
    }
  }
}

abstract class HydraQuoUseCaseSpec(useCases: HydraUseCase*) extends HydraUseCaseSpec(useCases, "quo")

abstract class HydraUnityUseCaseSpec(useCases: HydraUseCase*) extends HydraUseCaseSpec(useCases, "unity")
