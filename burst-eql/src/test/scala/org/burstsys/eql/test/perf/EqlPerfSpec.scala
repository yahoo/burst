/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.perf

import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.views.AlloyJsonFileView
import org.burstsys.alloy.alloy.views.AlloyJsonUseCaseViews.generateAlloy
import org.burstsys.alloy.views.unity.UnityGenerator.generateIterator
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.vitals.instrument.{prettyByteSizeString, prettyTimeFromNanos}
import org.burstsys.vitals.logging.log

import java.nio.file.Files

/**
  *   Performance and profiling queries
  */

final
class EqlPerfSpec extends EqlAlloyTestRunner {
  var view: AlloyJsonFileView = _
  override def localAfterStartup(): Unit = {
    log info s"Creating alloy dataset"
    view = generateAlloy(unitySchema, 6666, 6666, generateIterator(userCount = 200, sessionCount = 50, eventCount = 200, parameterCount = 1)).asInstanceOf[AlloyJsonFileView]
    log info s"Created JSON file ${view.source} of size ${prettyByteSizeString(Files.size(view.source))}"
  }

  override protected lazy val localViews: Array[AlloyView] = {
    super.localViews ++ Array(view)
  }

  val iterations = 10
  val parallelism = 1


  ignore should "do simple expression frequency" in {
    val source =
      """
         |select(st: long, et: long) count(user) as users,
         |      split(frequency(user.sessions, minute(user.sessions.startTime)), 0, 1, 2, 4, 6, 9, 14, 19, 10000000) as freq
         |      from schema Unity where user.sessions.startTime between $st and $et && user.sessions.sessionType != 1
         |      limit 270
       """.stripMargin

    runTest(source, 666, 666, { result =>
      val _ = checkResults(result)
    },
      s""" {"st": 0, "et":  ${System.currentTimeMillis()}}""",
      parallelism = parallelism, iterations = iterations)
  }

  it should "do simple counts" in {
    val source =
      """
        |select count(user) as users ,count(user.sessions) as sessions, count(user.sessions.events) as events
        |      from schema Unity
       """.stripMargin

    val avgTimeNanos = runTest(source, 6666, 6666, { result =>
      val _ = checkResults(result)
    }, parameters = "{}", parallelism = parallelism, iterations = iterations)

    log info s"Average time: ${prettyTimeFromNanos(avgTimeNanos)} for $parallelism parallel threads of $iterations iterations"
  }
}
