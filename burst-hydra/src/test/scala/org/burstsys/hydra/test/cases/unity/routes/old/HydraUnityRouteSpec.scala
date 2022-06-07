/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.routes.old

import org.burstsys.hydra.test.cases.support.{HydraUseCase, HydraUseCaseRunner}
import org.scalatest.Ignore

import scala.language.postfixOps

/**
  *
  */
@Ignore
class HydraUnityRouteSpec extends HydraUseCaseRunner {

  override def localStartup(): Unit = {
    super.localStartup()

  }

  val useCases: Array[HydraUseCase] = Array(
    HydraUnityRoute09Query
  )

  it should "test route code generation" in {
    useCases.foreach(runParallelTest(_))
  }

}
