/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.hydra.test.cases.support.{HydraUseCase, HydraUseCaseRunner}
import org.scalatest.Ignore

import scala.language.postfixOps

@Ignore
class HydraUnityBugsSpec extends HydraUseCaseRunner {

  override def localStartup(): Unit = {
    super.localStartup()

  }

  val useCases: Array[HydraUseCase] = Array(
//    HydraUnityBug00,
//      HydraUnityBug01,
//    HydraUnityBug02,
//      HydraUnityBug03,
//      HydraUnityBug04,
//    HydraUnityBug05,
    HydraUnityBug06
  )

  it should "execute unity bug cases" in {
    useCases.foreach(runParallelTest(_))
  }

}
