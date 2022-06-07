/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases

import org.burstsys.hydra.test.cases.support.HydraUseCaseRunner
import org.burstsys.hydra.test.cases.unity.counts.HydraUnityCase00
import org.burstsys.hydra.test.cases.unity.nested.HydraUnityCase13
import org.scalatest.Ignore

import scala.language.postfixOps

/**
 * used to support development (usually ignored)
 */
@Ignore
class HydraProfileUseCaseRunner extends HydraUseCaseRunner {

  override def localStartup(): Unit = {
    super.localStartup()
  }


  it should "execute use cases" in {
    runProfileTest(HydraUnityCase00)
  }

}
