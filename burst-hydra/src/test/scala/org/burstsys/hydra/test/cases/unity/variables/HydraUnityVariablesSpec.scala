/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.variables

import org.burstsys.hydra.test.cases.support.{HydraUseCase, HydraUseCaseRunner}
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
class HydraUnityVariablesSpec extends HydraUseCaseRunner {

  override def localStartup(): Unit = {
    super.localStartup()
  }

  val useCases: Array[HydraUseCase] = Array(
    HydraUnityVariablesCase00
  )

  it should "execute quo use cases" in {
    useCases.foreach(runSerialTest)
  }

}
