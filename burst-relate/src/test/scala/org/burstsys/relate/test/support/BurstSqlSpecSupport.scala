/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.test.support

import org.burstsys.relate.provider.RelateMockProvider
import org.burstsys.relate.test.RelateTestPersister
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.burstsys.vitals.properties.VitalsPropertyRegistry

trait BurstSqlSpecSupport extends BurstSqlSpecLog {

  VitalsMetricsRegistry.disable()

  def sqlTest(body: RelateTestPersister => Unit) {
    val sql = RelateMockProvider().start
    val persister = RelateTestPersister()
    sql.registerPersister(persister)
    persister.connection localTx {
      implicit session => persister.createTable
    }
    persister.connection localTx {
      implicit session => body(persister)
    }
    persister.connection localTx {
      implicit session => persister.dropTable
    }

  }

}
