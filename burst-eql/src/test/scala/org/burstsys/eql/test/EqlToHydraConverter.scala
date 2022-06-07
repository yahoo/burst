/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test

import org.burstsys.eql.test.support.EqlAlloyTestRunner

/**
 * used for hydra debugging.
 *
 */
//@Ignore
final
class EqlToHydraConverter extends EqlAlloyTestRunner {

  // We have to break the query up into parts right now since it breaks Hydra for size
  it should "convert eql to hydra" in {
    val source =
      s"""|
          |select(flurryId: string, startDate: string, endDate: string)
          |count(user.sessions.events) as eventCount,
          |user.sessions.id as sessionId,
          |user.sessions.osVersionId as osVersion,
          |user.sessions.appVersion.id as appVersionId,
          |user.sessions.carrierId as carrierId,
          |user.sessions.duration as sessionLength,
          |user.sessions.startTime as sessionStartTime
          |from schema Unity
          |where  (user.sessions.startTime > datetime($$startDate) && user.sessions.startTime < datetime($$endDate) && user.id == $$flurryId)
          |limit 100
          |""".stripMargin
    val hydra = toHydraSource(source)
    hydra
  }
}
