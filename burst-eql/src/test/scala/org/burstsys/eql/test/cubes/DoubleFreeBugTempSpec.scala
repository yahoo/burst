/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException
import org.scalatest.Ignore

/**
 * (Hydra Bug:  https://git.ouroath.com/burst/burst/issues/1720)
 */
@Ignore
final
class DoubleFreeBugTempSpec extends EqlAlloyTestRunner {

  it should "NOT CAUSE A DOUBLE FREE (IT DOES CURRENTLY SADLY)" in {
    val source =
      s"""
         | select user.sessions.events.parameters.key as id
         | beside select count(user) as 'count',
         | day(user.application.firstUse.sessionTime) as 'start',
         | day(user.application.lastUse.sessionTime) as 'end'
         | from schema Unity
       """.stripMargin

    runTest(source, 200, 200, { result =>
    })
  }
}
