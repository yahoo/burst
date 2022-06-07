/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.parameters

import org.burstsys.hydra.test.cases.support.{HydraUseCase, HydraUseCaseRunner}

import scala.language.postfixOps

//@Ignore
class HydraUnityParametersSpec extends HydraUseCaseRunner {

  override def localStartup(): Unit = {
    super.localStartup()
  }

  val useCases: Array[HydraUseCase] = Array(
    HydraUnityStringParameterCase,
    HydraUnityNullStringParameterCase,
    HydraUnityStringWithCallParameterCase
  )

  it should "execute quo use cases" in {
    useCases.foreach(runSerialTest)
  }

}
