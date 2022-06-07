/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada.test

import java.io.StringReader

import org.burstsys.torquemada.Driver
import org.burstsys.torquemada.Parameters.TorcherParameters
import org.apache.logging.log4j.Level

class TorquemadaBadParametersSpec extends TorquemadaHelper {
  it must "gracefully accept an empty torcher document" in {
    resetTestCounters()

    val params = TorcherParameters().copy(source = new StringReader(
      s"""|
          |
          |""".stripMargin
    ))

    var errorSeen = false
    val driver = Driver(params, agentClient, catalogClient).addListener { (l, m) =>
      if (l == Level.ERROR) errorSeen = true
      log.log(l, m)
    }.start
    driver.run

    assert(!driver.isRunning)
    assert(errorSeen)
  }


  it must "gracefully accept a malformed torcher document" in {
    resetTestCounters()

    val params = TorcherParameters().copy(source = new StringReader(
      """|{
         |    "storeType": "sample",
         |    "schemaName": "quo",
         |    "duration": "2 hours",
         |    "parallelism": 5,
         |    "queries": [
         |        "select count(user) as users, count(user.events) as events from schema quo",
         |        "select count(user) as frequency, user.deviceModelId as deviceIds\nbeside select count(user.sessions) as frequency, user.sessions.osVersion as firmwareIds\nbeside select count(user.project) as frequency, user.project.languageId as languageIds\nbeside select count(user) as frequency, user.sessions.appVersionId as versionIds\nbeside select count(user.sessions.events) as frequency, user.sessions.events.eventId as eventIds\nfrom schema quo limit 5000"
         |    ],
         |    "batches": [
         |        { "domains": [
         |            { "pk": 317564 },
         |            { "pk": 315604 },
         |""".stripMargin
      ))

    var errorSeen = false
    val driver = Driver(params, agentClient, catalogClient).addListener { (l, m) =>
      if (l == Level.ERROR) errorSeen = true
      log.log(l, m)
    }.start
    driver.run

    assert(!driver.isRunning)
    assert(errorSeen)
  }

  it must "gracefully accept a torcher document that matches nothing" in {
    resetTestCounters()

    val params = TorcherParameters().copy(source = new StringReader(
      """|{
         |    "storeType": "sample",
         |    "schemaName": "quo",
         |    "duration": "2 hours",
         |    "parallelism": 5,
         |    "queries": [
         |        "select count(user) as users, count(user.events) as events from schema quo",
         |        "select count(user) as frequency, user.deviceModelId as deviceIds\nbeside select count(user.sessions) as frequency, user.sessions.osVersion as firmwareIds\nbeside select count(user.project) as frequency, user.project.languageId as languageIds\nbeside select count(user) as frequency, user.sessions.appVersionId as versionIds\nbeside select count(user.sessions.events) as frequency, user.sessions.events.eventId as eventIds\nfrom schema quo limit 5000"
         |    ],
         |    "batches": [
         |        { "domains": [
         |            { "pk": 1 }
         |        ]}
         |    ]
         |}
         |""".stripMargin
    ))

    var warnSeen = false
    val driver = Driver(params, agentClient, catalogClient).addListener { (l, m) =>
      if (l == Level.WARN) warnSeen = true
      log.log(l, m)
    }.start
    driver.run

    assert(!driver.isRunning)
    assert(warnSeen)
  }
}
